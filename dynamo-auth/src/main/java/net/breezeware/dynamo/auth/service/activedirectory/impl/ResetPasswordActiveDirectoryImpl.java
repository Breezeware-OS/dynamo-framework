package net.breezeware.dynamo.auth.service.activedirectory.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.auth.service.api.ResetPasswordService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("active-directory")
public class ResetPasswordActiveDirectoryImpl extends ResetPasswordService {
    @Override
    public void completeResetPasswordRequest(String resetPasswordRequestId, String password) throws DynamoException {
        throw new DynamoException("Method not implemented.", HttpStatus.NOT_IMPLEMENTED);
    }
}
