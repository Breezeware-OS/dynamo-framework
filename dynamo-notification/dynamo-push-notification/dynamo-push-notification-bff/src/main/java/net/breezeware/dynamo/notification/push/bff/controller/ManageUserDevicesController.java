package net.breezeware.dynamo.notification.push.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.breezeware.dynamo.notification.push.bff.dto.UserDeviceMapDto;
import net.breezeware.dynamo.notification.push.bff.service.ManageUserDeviceService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ErrorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/service/push-notifications/manage-user-devices")
public class ManageUserDevicesController {

    private final ManageUserDeviceService manageUserDeviceService;

    @Operation(method = "POST", summary = "Registers a user device map",
            description = "Registers a user device map and generates endpoint based on the device type")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDeviceMapDto.class)))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success Payload"),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))) })
    @PostMapping
    public UserDeviceMapDto registerUserDeviceMap(@RequestBody UserDeviceMapDto userDeviceMapDto) {
        log.info("Entering registerUserDeviceMap()");
        userDeviceMapDto = manageUserDeviceService.registerUserDeviceMap(userDeviceMapDto);
        log.info("Leaving registerUserDeviceMap()");
        return userDeviceMapDto;
    }

    @Operation(method = "DELETE", summary = "Deletes the user device map",
            description = "Deletes the user device map by device token")
    @Parameters(value = { @Parameter(allowEmptyValue = false, required = true, name = "device-token",
            description = "Device token.", in = ParameterIn.PATH) })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success Payload"),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "405", description = "Method Not Allowed",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))) })
    @DeleteMapping(value = "/user-device/{device-token}")
    public void deleteUserDeviceMapByDeviceToken(
            @PathVariable(name = "device-token", required = true) String deviceToken) throws DynamoException {
        log.info("Entering deleteUserDeviceMapByDeviceToken");
        manageUserDeviceService.deleteUserDeviceMap(deviceToken);
        log.info("Leaving deleteUserDeviceMapByDeviceToken");
    }
}
