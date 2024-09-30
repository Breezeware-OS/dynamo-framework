package net.breezeware.dynamo.auth.service.fusionauth.impl;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;

import net.breezeware.dynamo.auth.cache.ResetPasswordDetailsCacheConfig;
import net.breezeware.dynamo.auth.dto.ResetPasswordDetails;
import net.breezeware.dynamo.auth.service.api.ResetPasswordService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.user.ChangePasswordRequest;

@Slf4j
@Service
@Profile("fusionauth")
public class ResetPasswordFusionAuthImpl extends ResetPasswordService {

    @Autowired
    ResetPasswordDetailsCacheConfig resetPasswordDetailsCacheConfig;

    @Autowired
    FusionAuthClient fusionAuthClient;

    public void completeResetPasswordRequest(String resetPasswordRequestId, String password) throws DynamoException {
        log.info("Entering completeResetPasswordRequest()");
        if (resetPasswordRequestId == null || resetPasswordRequestId.isEmpty() || resetPasswordRequestId.isBlank()) {
            throw new DynamoException("Invalid reset password request id.", HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.isEmpty() || password.isBlank()) {
            throw new DynamoException("Invalid password.", HttpStatus.BAD_REQUEST);
        }

        Cache<String, ResetPasswordDetails> resetPasswordDetailsCache =
                resetPasswordDetailsCacheConfig.getResetPasswordDetailsCache();
        if (!resetPasswordDetailsCache.containsKey(resetPasswordRequestId)) {
            throw new DynamoException("Unable to reset password. Please try again.", HttpStatus.BAD_REQUEST);
        }

        ResetPasswordDetails resetPasswordDetails = resetPasswordDetailsCache.get(resetPasswordRequestId);
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.loginId = resetPasswordDetails.getEmail();
        changePasswordRequest.password = password;
        ClientResponse<Void, Errors> response = fusionAuthClient.changePasswordByIdentity(changePasswordRequest);
        if (!response.wasSuccessful()) {
            buildErrorResponse(response.errorResponse);
        }

        log.info("Leaving completeResetPasswordRequest()");
    }

    /**
     * Builds error response.
     * @param  errorResponse   Standard error domain object that can also be used as
     *                         the response from an API call.
     * @throws DynamoException user-defined exception.
     */
    private void buildErrorResponse(Errors errorResponse) throws DynamoException {
        log.info("Entering buildErrorResponse()");
        // Uses for updating local variable inside lambda function
        var wrapperString = new Object() {
            String errorString = "";
        };
        // If error occurs
        if (errorResponse != null) {
            // If any field error occurs
            if (errorResponse.fieldErrors != null) {
                errorResponse.fieldErrors.keySet().forEach(fieldError -> {
                    wrapperString.errorString += errorResponse.fieldErrors.get(fieldError).get(0).message + ",";
                });
            }

            // If any general error occurs
            if (errorResponse.generalErrors.size() > 0) {
                errorResponse.generalErrors.forEach(generalError -> {
                    wrapperString.errorString += generalError.message + ",";
                });
            }

            if (!wrapperString.errorString.isBlank()) {
                throw new DynamoException(wrapperString.errorString, HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildErrorResponse()");

    }
}
