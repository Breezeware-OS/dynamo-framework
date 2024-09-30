package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonvalidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * A custom annotation for marking fields that should be validated as JSONNode
 * objects. Fields annotated with @ValidJsonNode will be checked to ensure they
 * are not null and not representing a null value.
 */
@Documented
@Constraint(validatedBy = JsonNodeValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidJsonNode {
    /**
     * Specifies the error message to be used when validation fails.
     * @return the error message.
     */
    String message() default "Invalid JSON";

    /**
     * Specifies validation groups. Not used in this annotation by default.
     * @return an array of validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Specifies payload classes. Not used in this annotation by default.
     * @return an array of payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
