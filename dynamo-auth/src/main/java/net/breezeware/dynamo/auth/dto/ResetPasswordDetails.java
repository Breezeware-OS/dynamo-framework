package net.breezeware.dynamo.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entity to hold details necessary to reset a user's password.
 */
@Data
public class ResetPasswordDetails {
    /**
     * User's email address.
     */
    @Schema(example = "johnDoe@gmail.com", description = "User's email address.")
    @NotNull(message = "Missing user's email address")
    @Pattern(regexp = "^$|^.+@.+\\..+", message = "Provide valid email address")
    private String email;

    /**
     * One-time password (OTP) is a four digit numeric that authenticates a user for
     * a single transaction.
     */
    @Schema(example = "1234", description = """
            One-time password (OTP) is a four digit numeric \
            that authenticates a user for a single transaction.\
            """)
    private String otp;
}
