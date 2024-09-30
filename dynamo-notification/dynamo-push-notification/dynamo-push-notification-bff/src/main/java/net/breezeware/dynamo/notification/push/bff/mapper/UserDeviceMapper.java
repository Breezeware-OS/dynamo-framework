package net.breezeware.dynamo.notification.push.bff.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import net.breezeware.dynamo.notification.push.bff.dto.UserDeviceMapDto;
import net.breezeware.dynamo.notification.push.svc.entity.UserDeviceMap;

@Mapper(componentModel = "spring")
public interface UserDeviceMapper {

    @Mappings(value = { @Mapping(source = "userId", target = "userId"),
        @Mapping(source = "platformType", target = "platformType"),
        @Mapping(source = "deviceToken", target = "deviceToken") })
    UserDeviceMap userDeviceMapDtoToUserDeviceMap(UserDeviceMapDto userDeviceMapDto);

    UserDeviceMapDto userDeviceMapToUserDeviceMapDto(UserDeviceMap userDeviceMap);
}
