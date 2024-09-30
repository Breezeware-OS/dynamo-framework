package net.breezeware.dynamo.auth.service.activedirectory.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

import net.breezeware.dynamo.auth.dto.AuthUser;
import net.breezeware.dynamo.auth.service.activedirectory.properties.ActiveDirectoryProperties;
import net.breezeware.dynamo.auth.service.api.UserManagementService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("active-directory")
public class UserManagementActiveDirectoryServiceImpl implements UserManagementService {

    @Autowired
    @Qualifier("ResourceServerGraphClient")
    GraphServiceClient graphServiceClient;

    @Autowired
    ActiveDirectoryProperties activeDirectoryProperties;

    @Override
    public Optional<AuthUser> retrieveUserByEmail(String email) throws DynamoException {
        log.info("Entering retrieveUserByEmail()");
        try {
            User user = graphServiceClient.users(convertEmailToUserPrincipalName(email)).buildRequest().get();
            AuthUser authUser = new AuthUser();
            authUser.setEmail(user.mail != null ? user.mail : "");
            authUser.setFirstName(user.givenName != null ? user.givenName : "");
            authUser.setLastName(user.surname != null ? user.surname : "");
            authUser.setPhoneNumber(user.mobilePhone != null ? user.mobilePhone : "");
            log.info("Leaving retrieveUserByEmail()");
            return Optional.of(authUser);
        } catch (ClientException e) {
            return Optional.empty();
        }

    }

    /**
     * Convert email string to unique user principal name.
     * @param  email           email string value.
     * @return                 converted email to user principal value.
     * @throws DynamoException throws exception if string has null or empty values.
     */
    private String convertEmailToUserPrincipalName(String email) throws DynamoException {
        log.info("Entering convertEmailToUserPrincipalName()");
        if (email == null || email.isBlank() || email.isEmpty()) {
            throw new DynamoException("Invalid email address.", HttpStatus.BAD_REQUEST);
        }

        String userNameValue = email.replaceAll("@", "_").trim();
        userNameValue += activeDirectoryProperties.getDomainName();
        log.info("Leaving convertEmailToUserPrincipalName()");
        return userNameValue;
    }
}
