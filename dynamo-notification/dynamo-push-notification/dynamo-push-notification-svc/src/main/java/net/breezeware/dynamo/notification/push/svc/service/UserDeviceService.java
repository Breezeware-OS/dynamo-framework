package net.breezeware.dynamo.notification.push.svc.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.notification.push.svc.dao.UserDeviceMapRepository;
import net.breezeware.dynamo.notification.push.svc.entity.UserDeviceMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserDeviceService extends GenericService<UserDeviceMap> {

    private final UserDeviceMapRepository userDeviceMapRepository;

    public UserDeviceService(UserDeviceMapRepository userDeviceMapRepository) {
        super(userDeviceMapRepository);
        this.userDeviceMapRepository = userDeviceMapRepository;

    }

    /**
     * Retrieves a {@code UserDeviceMap} entity by its device token.
     * @param  deviceToken The device token used to identify the user device map.
     * @return             An {@code Optional} containing the {@code UserDeviceMap}
     *                     entity associated with the provided device token, or an
     *                     empty {@code Optional} if no such entity exists.
     */
    public Optional<UserDeviceMap> retrieveUserDeviceMapByDeviceToken(String deviceToken) {
        log.info("Entering retrieveUserDeviceMapByDeviceToken");
        Optional<UserDeviceMap> optionalUserDeviceMap = userDeviceMapRepository.findByDeviceToken(deviceToken);
        log.info("Leaving retrieveUserDeviceMapByDeviceToken");
        return optionalUserDeviceMap;
    }
}
