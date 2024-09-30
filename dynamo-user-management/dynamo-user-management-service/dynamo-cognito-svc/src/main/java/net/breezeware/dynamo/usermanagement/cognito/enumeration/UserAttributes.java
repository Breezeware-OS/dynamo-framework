package net.breezeware.dynamo.usermanagement.cognito.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum represents the attributes of a user.
 */
@Getter
@AllArgsConstructor
public enum UserAttributes {
    /**
     * The user unique id attribute of a user.
     */
    USER_ID("custom:user_id"),
    /**
     * The email attribute of a user.
     */
    EMAIL("email"),
    /**
     * The phone number attribute of a user.
     */
    PHONE_NUMBER("phone_number"),
    /**
     * The given name attribute of a user.
     */
    GIVEN_NAME("given_name"),
    /**
     * The family name attribute of a user.
     */
    FAMILY_NAME("family_name"),
    /**
     * The email verification status attribute of a user.
     */
    EMAIL_VERIFIED("email_verified"),
    /**
     * The phone number verification status attribute of a user.
     */
    PHONE_NUMBER_VERIFIED("phone_number_verified"),
    /**
     * The name of the application associated with a user.
     */
    APPLICATION_NAME("custom:application_name");

    /**
     * The name of the attribute.
     */
    public final String name;

}
