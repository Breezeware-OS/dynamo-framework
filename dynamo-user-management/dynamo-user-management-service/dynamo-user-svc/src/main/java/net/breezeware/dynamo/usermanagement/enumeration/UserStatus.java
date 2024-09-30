package net.breezeware.dynamo.usermanagement.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * An enumeration representing the status of a user account.
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum UserStatus {

    /**
     * The user account is active.
     */
    ACTIVE("active"),

    /**
     * The user account is suspended.
     */
    SUSPENDED("suspended"),

    /**
     * The user account has been invited but not yet activated.
     */
    INVITED("invited");

    /**
     * The string representation of the user status.
     */
    public final String status;

    /**
     * Retrieves an Optional containing the UserStatus for the given value.
     * @param  value The string value representing the user status.
     * @return       An Optional containing the corresponding UserStatus, if
     *               available.
     */
    public static Optional<UserStatus> retrieveUserStatus(String value) {
        log.info("Entering retrieveUserStatus(), value: {}", value);
        Optional<UserStatus> optionalUserStatus =
                Arrays.stream(values()).filter(ts -> ts.status.equalsIgnoreCase(value)).findFirst();
        log.info("Leaving retrieveUserStatus(), is UserStatus available for value: {} = {}", value,
                optionalUserStatus.isPresent());
        return optionalUserStatus;
    }

    /**
     * Retrieves a list of all available session status values.
     * @return A list of strings representing all available UserStatus values.
     */
    public static List<String> retrieveAllUserStatus() {
        log.info("Entering retrieveAllUserStatus()");
        List<String> userStatusList = Arrays.stream(values()).map(UserStatus::getStatus).toList();
        log.info("Leaving retrieveAllUserStatus(), # of available UserStatus status: {}", userStatusList.size());
        return userStatusList;
    }
}
