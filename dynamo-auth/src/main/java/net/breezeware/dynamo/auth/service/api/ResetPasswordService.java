package net.breezeware.dynamo.auth.service.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import net.breezeware.dynamo.auth.cache.ResetPasswordDetailsCacheConfig;
import net.breezeware.dynamo.auth.dto.AuthUser;
import net.breezeware.dynamo.auth.dto.ResetPasswordDetails;
import net.breezeware.dynamo.aws.ses.exception.DynamoSesException;
import net.breezeware.dynamo.aws.ses.service.api.SesService;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ResetPasswordService {

    @Autowired
    ResetPasswordDetailsCacheConfig resetPasswordDetailsCacheConfig;
    @Autowired
    SesService sesService;
    @Value("${from.email.address}")
    String fromEmailAddress;
    @Value("${forgot-password-email-template-name}")
    String forgotPasswordEmailTemplateName;
    @Autowired
    UserManagementService userManagementService;

    /**
     * Validates whether provided email address already exists. If the email address
     * doesn't exist, throw an exception. If an email address exists, Generate a
     * 4-digit OTP and email it to the user. The OTP and email will be stored in a
     * cache or permanent storage for later use.
     * @param  email           user's email address.
     * @return                 resetPasswordRequestId Unique ID generated for this
     *                         request. This ID will be used to complete the reset
     *                         password process after the OTP is validated using the
     *                         {@link #validateResetPasswordOtp(String, String)}
     *                         method.
     * @throws DynamoException throws an exception if the provided email address is
     *                         invalid.
     */
    public String validateResetPasswordRequest(String email) throws DynamoException {
        log.info("Entering validatesResetPasswordRequest()");
        if (email == null || email.isBlank() || email.isEmpty()) {
            throw new DynamoException("Invalid email address.", HttpStatus.NOT_FOUND);
        }

        String otp = generateFourDigitRandomOtp();
        ResetPasswordDetails resetPasswordDetails = new ResetPasswordDetails();
        resetPasswordDetails.setEmail(email);
        resetPasswordDetails.setOtp(otp);

        String resetPasswordRequestId = String.valueOf(UUID.randomUUID());
        storeResetPasswordDetailsInCache(resetPasswordRequestId, resetPasswordDetails);
        Optional<AuthUser> authUser = userManagementService.retrieveUserByEmail(email);
        if (authUser.isEmpty()) {
            throw new DynamoException("Email address not found.", HttpStatus.NOT_FOUND);
        }

        sendTemplatedEmail(email, otp, authUser.get().getFirstName(), authUser.get().getLastName());

        log.info("Leaving validatesResetPasswordRequest()");
        return resetPasswordRequestId;
    }

    /**
     * Generates one time password(OTP).
     * @return one time password(OTP).
     */
    private String generateFourDigitRandomOtp() {
        log.info("Entering generateFourDigitRandomOtp()");
        int randomValue = new Random().nextInt(9999);
        String formattedFourDigitRandomValueString = "%04d".formatted(randomValue);
        log.info("Leaving generateFourDigitRandomOtp() {}", formattedFourDigitRandomValueString);
        return formattedFourDigitRandomValueString;
    }

    /**
     * Stores reset password details in cache.
     * @param resetPasswordRequestId The Unique ID used to store email and OTP in
     *                               the cache.
     * @param resetPasswordDetails   Stores email and OTP in the cache.
     */
    private void storeResetPasswordDetailsInCache(String resetPasswordRequestId,
            ResetPasswordDetails resetPasswordDetails) {
        log.info("Entering storesResetPasswordDetailsInCache()");
        resetPasswordDetailsCacheConfig.getResetPasswordDetailsCache().put(resetPasswordRequestId,
                resetPasswordDetails);
        log.info("Leaving storesResetPasswordDetailsInCache()");
    }

    /**
     * Sends an email to registered email address.
     * @param  toAddress       The recipients to place on the To: line of the
     *                         message.
     * @param  otp             One-time password (OTP) is a four digit numeric that
     *                         authenticates a user for a single transaction.
     * @param  firstName       User's first name.
     * @param  lastName        User's last name.
     * @throws DynamoException Throws Mail not send exceptions.
     */
    private void sendTemplatedEmail(String toAddress, String otp, String firstName, String lastName)
            throws DynamoException {
        log.info("Entering sendTemplatedEmail()");
        Map<String, Object> templateDataMap = new HashMap<>();
        templateDataMap.put("otp", otp);
        templateDataMap.put("first-name", firstName);
        templateDataMap.put("last-name", lastName);
        try {
            sesService.sendTemplatedEmail(fromEmailAddress, toAddress, forgotPasswordEmailTemplateName,
                    templateDataMap);
        } catch (DynamoSesException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving sendTemplatedEmail()");
    }

    /**
     * Validates the OTP generated by the method
     * {@link #validateResetPasswordRequest(String)}.
     * @param  resetPasswordRequestId the Unique ID from the method
     *                                {@link #validateResetPasswordRequest(String)},
     *                                for this request to retrieve OTP from cache or
     *                                permanent storage.
     * @param  otp                    used to verify the provided OTP is valid.
     * @throws DynamoException        throws exception if request has an invalid
     *                                resetPasswordRequestId or otp value.
     */
    public void validateResetPasswordOtp(String resetPasswordRequestId, String otp) throws DynamoException {
        log.info("Entering validatesResetPasswordOtp()");
        Cache<String, ResetPasswordDetails> resetPasswordDetailsCache =
                resetPasswordDetailsCacheConfig.getResetPasswordDetailsCache();
        if (!resetPasswordDetailsCache.containsKey(resetPasswordRequestId)) {
            throw new DynamoException("Unable to validate OTP. Please try again.", HttpStatus.BAD_REQUEST);
        }

        // Checks whether the provided OTP is valid
        ResetPasswordDetails resetPasswordDetails = resetPasswordDetailsCache.get(resetPasswordRequestId);
        if (!resetPasswordDetails.getOtp().equals(otp)) {
            throw new DynamoException("Invalid OTP.", HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validatesResetPasswordOtp()");

    }

    /**
     * Completes the reset password request by updating the user's password.
     * @param  resetPasswordRequestId the Unique ID from the method
     *                                {@link #validateResetPasswordRequest(String)},
     *                                for this request to retrieve email address
     *                                from cache or permanent storage.
     * @param  password               the password that needs to be updated.
     * @throws DynamoException        throws exception if request have invalid
     *                                resetPasswordRequestId or password value.
     */
    public abstract void completeResetPasswordRequest(String resetPasswordRequestId, String password)
            throws DynamoException;

    /**
     * Regenerates OTP for user provided email address. Email address will be
     * retrieved from the cache or permanent storage.
     * @param  resetPasswordRequestId the Unique ID from the method
     *                                {@link #validateResetPasswordRequest(String)},
     *                                for this request to retrieve email address
     *                                from cache or permanent storage.
     * @throws DynamoException        throws exception if request have invalid
     *                                resetPasswordRequestId.
     */
    public void regenerateAndEmailOtp(String resetPasswordRequestId) throws DynamoException {
        log.info("Entering regenerateAndEmailOtp()");
        if (resetPasswordRequestId == null || resetPasswordRequestId.isEmpty() || resetPasswordRequestId.isBlank()) {
            throw new DynamoException("Invalid reset password request id.", HttpStatus.BAD_REQUEST);
        }

        Cache<String, ResetPasswordDetails> resetPasswordDetailsCache =
                resetPasswordDetailsCacheConfig.getResetPasswordDetailsCache();
        if (!resetPasswordDetailsCache.containsKey(resetPasswordRequestId)) {
            throw new DynamoException("Unable to regenerate OTP. Please try again.", HttpStatus.BAD_REQUEST);
        }

        ResetPasswordDetails resetPasswordDetails = resetPasswordDetailsCache.get(resetPasswordRequestId);
        String otp = generateFourDigitRandomOtp();
        resetPasswordDetails.setOtp(otp);

        storeResetPasswordDetailsInCache(resetPasswordRequestId, resetPasswordDetails);
        Optional<AuthUser> authUser = userManagementService.retrieveUserByEmail(resetPasswordDetails.getEmail());
        if (authUser.isEmpty()) {
            throw new DynamoException("Email address not found.", HttpStatus.NOT_FOUND);
        }

        sendTemplatedEmail(resetPasswordDetails.getEmail(), otp, authUser.get().getFirstName(),
                authUser.get().getLastName());

        log.info("Leaving regenerateAndEmailOtp()");

    }

}
