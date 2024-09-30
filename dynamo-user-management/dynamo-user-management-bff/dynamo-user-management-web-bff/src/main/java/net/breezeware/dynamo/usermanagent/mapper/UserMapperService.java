package net.breezeware.dynamo.usermanagent.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import net.breezeware.dynamo.usermanagement.dto.SignedUpUserData;
import net.breezeware.dynamo.usermanagement.dto.UserViewResponse;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;

@Mapper(componentModel = "spring")
public interface UserMapperService {

    /**
     * Maps {@link User} object to a {@link UserViewResponse} object.
     * @param  user the {{@link User}} object to be mapped
     * @return      the mapped {@link UserViewResponse} object.
     */

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "userId", source = "uniqueId")
    UserViewResponse userToUserViewResponse(User user);

    @Mappings(value = { @Mapping(target = "idmUserId", source = "signedUpUserData.idmUniqueUserId"),
        @Mapping(target = "idmInfo", source = "idmInfo"),
        @Mapping(target = "firstName", source = "signedUpUserData.firstName"),
        @Mapping(target = "lastName", source = "signedUpUserData.lastName"),
        @Mapping(target = "email", source = "signedUpUserData.email"),
        @Mapping(target = "phoneNumber", source = "signedUpUserData.phoneNumber"), @Mapping(target = "status",
                source = "signedUpUserData.idmUserStatus", qualifiedByName = "idmStatusToUserStatus") })
    User signedUpUserDataToUser(SignedUpUserData signedUpUserData, IdmInfo idmInfo);

    @Named("idmStatusToUserStatus")
    static String idmStatusToUserStatus(String idmStatus) {
        return UserStatus.retrieveAllUserStatus().contains(idmStatus) ? UserStatus.retrieveUserStatus(idmStatus).get()
                .getStatus()
                : List.of("confirmed", "approved").contains(idmStatus.toLowerCase()) ? UserStatus.ACTIVE.getStatus()
                : UserStatus.INVITED.getStatus();
    }

}
