package net.breezeware.dynamo.auth.service.fusionauth.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;

import net.breezeware.dynamo.auth.dto.AuthUser;
import net.breezeware.dynamo.auth.service.api.UserManagementService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;

@Service
@Slf4j
@Profile("fusionauth")
public class UserManagementFusionAuthServiceImpl implements UserManagementService {
    @Autowired
    FusionAuthClient fusionAuthClient;

    @Override
    public Optional<AuthUser> retrieveUserByEmail(String email) throws DynamoException {
        log.info("Entering retrieveUserProfileByEmail()");
        ClientResponse<UserResponse, Errors> userResponseErrorsClientResponse =
                fusionAuthClient.retrieveUserByEmail(email);
        if (!userResponseErrorsClientResponse.wasSuccessful()) {
            return Optional.empty();
        }

        AuthUser authUser = new AuthUser();
        User user = userResponseErrorsClientResponse.successResponse.user;
        authUser.setEmail(user.email);
        authUser.setFirstName(user.firstName);
        authUser.setLastName(user.lastName);
        authUser.setPhoneNumber(user.mobilePhone);
        log.info("Leaving retrieveUserProfileByEmail()");
        return Optional.of(authUser);
    }
}
