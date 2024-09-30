package net.breezeware.dynamo.notification.push.bff.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.aws.sns.exception.DynamoSnsException;
import net.breezeware.dynamo.aws.sns.service.api.SnsService;
import net.breezeware.dynamo.notification.push.bff.dto.UserDeviceMapDto;
import net.breezeware.dynamo.notification.push.bff.mapper.UserDeviceMapper;
import net.breezeware.dynamo.notification.push.svc.entity.UserDeviceMap;
import net.breezeware.dynamo.notification.push.svc.enumeration.MessageType;
import net.breezeware.dynamo.notification.push.svc.service.UserDeviceService;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageUserDeviceService {

    private final UserDeviceMapper userDeviceMapper;
    private final UserDeviceService userDeviceService;
    private final SnsService snsService;
    private final UserService userService;
    private final Validator fieldValidator;

    @Value("${platform.application.arn.gcm}")
    private String gcmPlatformApplicationArn;
    @Value("${platform.application.arn.apns}")
    private String apnsPlatformApplicationArn;
    @Value("${message.type.apns}")
    private String messageTypeApns;

    /**
     * Registers a user-device mapping based on the provided
     * {@code UserDeviceMapDto}.
     * @param  userDeviceMapDto                       The data transfer object
     *                                                containing information for
     *                                                registering the user-device
     *                                                mapping.
     * @return                                        The data transfer object
     *                                                representing the registered
     *                                                user-device mapping.
     * @throws jakarta.validation.ValidationException If the input data violates
     *                                                validation constraints.
     * @throws DynamoException                        If the user does not exist, or
     *                                                if there are issues while
     *                                                creating the platform
     *                                                endpoint, or if the device
     *                                                already exists, indicating a
     *                                                conflict with an existing
     *                                                device.
     */
    @Transactional
    public UserDeviceMapDto registerUserDeviceMap(UserDeviceMapDto userDeviceMapDto) {
        log.info("Entering registerUserDeviceMap()");

        // validate input
        Set<ConstraintViolation<UserDeviceMapDto>> violations = fieldValidator.validate(userDeviceMapDto);
        ValidationExceptionUtils.handleException(violations);

        // verify if user exists
        UUID userId = userDeviceMapDto.getUserId();
        userService.retrieveUser(userId)
                .orElseThrow(() -> new DynamoException(String.format("User with id: %s does not exist", userId),
                        HttpStatus.BAD_REQUEST));

        // verify if device already exists
        userDeviceService.retrieveUserDeviceMapByDeviceToken(userDeviceMapDto.getDeviceToken()).ifPresent(udm -> {
            throw new DynamoException(String.format("Device with token: %s already exists", udm.getDeviceToken()),
                    HttpStatus.CONFLICT);
        });

        // map dto to entity
        UserDeviceMap userDeviceMap = userDeviceMapper.userDeviceMapDtoToUserDeviceMap(userDeviceMapDto);
        userDeviceMap.setUniqueId(UUID.randomUUID());

        try {
            // create platform endpoint based on device type
            if (userDeviceMap.getPlatformType().equalsIgnoreCase(MessageType.GCM.type)) {
                String platformEndpoint =
                        snsService.createPlatformEndpoint(userDeviceMapDto.getDeviceToken(), gcmPlatformApplicationArn);
                userDeviceMap.setTargetEndpoint(platformEndpoint);
            } else if (userDeviceMap.getPlatformType().equalsIgnoreCase(MessageType.APNS.type)) {
                String platformEndpoint = snsService.createPlatformEndpoint(userDeviceMapDto.getDeviceToken(),
                        apnsPlatformApplicationArn);
                userDeviceMap.setTargetEndpoint(platformEndpoint);
                userDeviceMap.setPlatformType(messageTypeApns);
            } else if (userDeviceMap.getPlatformType().equalsIgnoreCase(MessageType.WEB.type)) {
                userDeviceMap.setTargetEndpoint("https://fcm.googleapis.com/fcm/send");
            }

        } catch (DynamoSnsException e) {
            throw new DynamoException("Error while creating platform endpoint.", HttpStatus.BAD_REQUEST);
        }

        userDeviceMap = userDeviceService.create(userDeviceMap);
        userDeviceMapDto = userDeviceMapper.userDeviceMapToUserDeviceMapDto(userDeviceMap);

        log.info("Leaving registerUserDeviceMap()");
        return userDeviceMapDto;
    }

    /**
     * Deletes the user-device mapping associated with the provided device token.
     * @param  deviceToken     The device token used to identify the user-device
     *                         mapping to be deleted.
     * @throws DynamoException If no user-device mapping exists with the provided
     *                         device token, indicating that the device token is not
     *                         associated with any existing mapping.
     */
    @Transactional
    public void deleteUserDeviceMap(String deviceToken) {
        log.info("Entering deleteUserDeviceMap");

        // verify if device already exists
        UserDeviceMap userDeviceMap = userDeviceService.retrieveUserDeviceMapByDeviceToken(deviceToken)
                .orElseThrow(() -> new DynamoException(String.format("No device with token: %s exists", deviceToken),
                        HttpStatus.NOT_FOUND));

        if (!userDeviceMap.getPlatformType().equalsIgnoreCase(MessageType.WEB.type)) {
            try {
                snsService.deletePlatformEndpoint(userDeviceMap.getTargetEndpoint());
            } catch (DynamoSnsException e) {
                throw new DynamoException("Error while deleting platform endpoint.", HttpStatus.BAD_REQUEST);
            }

        }

        // delete user-device registrations
        userDeviceService.delete(userDeviceMap.getId());

        log.info("Leaving deleteUserDeviceMap");
    }
}
