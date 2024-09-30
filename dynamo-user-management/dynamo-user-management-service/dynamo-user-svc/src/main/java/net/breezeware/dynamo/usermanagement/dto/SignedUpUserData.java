package net.breezeware.dynamo.usermanagement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignedUpUserData {

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private Boolean isEmailVerified;
    private String phoneNumber;
    private String organization;
    private List<String> roles;
    private List<String> groups;
    private String idmUserStatus;
    private String idmUniqueUserId;
    private String idmId;
    private String idmName;
}
