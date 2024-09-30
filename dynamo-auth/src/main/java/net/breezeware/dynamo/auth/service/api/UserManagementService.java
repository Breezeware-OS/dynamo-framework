package net.breezeware.dynamo.auth.service.api;

import java.util.Optional;

import net.breezeware.dynamo.auth.dto.AuthUser;
import net.breezeware.dynamo.utils.exception.DynamoException;

public interface UserManagementService {

    /**
     * Retrieves the user by the given email address.
     * @param  email           The email address of the user.
     * @return                 The {@link AuthUser} object.
     * @throws DynamoException Throws exception if provided email address is
     *                         invalid.
     */
    Optional<AuthUser> retrieveUserByEmail(String email) throws DynamoException;
}
