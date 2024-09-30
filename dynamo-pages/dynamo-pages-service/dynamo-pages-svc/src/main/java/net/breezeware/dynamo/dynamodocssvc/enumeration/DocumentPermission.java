package net.breezeware.dynamo.dynamodocssvc.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enumeration representing the permissions associated with accessing a
 * document. This enum defines various levels of permissions, such as
 * READ_WRITE, READ, and NO_ACCESS.
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum DocumentPermission {

    /**
     * Permission to read and write the document.
     */
    READ_WRITE("read_write"),

    /**
     * Permission to read the document.
     */
    READ("read"),

    /**
     * No access permission to the document.
     */
    NO_ACCESS("no_access");

    private final String value;

    /**
     * Retrieves the Permission enum based on the given string value.
     * @param  value The string value representing the permission.
     * @return       An Optional containing the corresponding Permission enum if
     *               found, or empty if not found.
     */
    public static Optional<DocumentPermission> retrievePermission(String value) {
        log.info("Entering retrievePermission(), value: {}", value);
        Optional<DocumentPermission> optionalPermission = Arrays.stream(values())
                .filter(documentPermission -> documentPermission.value.equalsIgnoreCase(value.toLowerCase()))
                .findFirst();
        log.info("Leaving retrievePermission(), is Permission available for value: {} = {}", value,
                optionalPermission.isPresent());
        return optionalPermission;
    }

    /**
     * Retrieves a list of all permission values.
     * @return A list containing all permission values.
     */
    public static List<String> retrieveAllPermissions() {
        log.info("Entering retrieveAllPermissions()");
        List<String> permissionList =
                Arrays.stream(values()).map(DocumentPermission::getValue).toList();
        log.info("Leaving retrieveAllPermissions(), # of available Permission: {}", permissionList.size());
        return permissionList;
    }
}
