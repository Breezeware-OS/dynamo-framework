package net.breezeware.dynamo.utils.annotations;

import java.util.Objects;
import java.util.UUID;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UuidValidator implements ConstraintValidator<ValidUuid, UUID> {

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return Objects.nonNull(value) && isValidUuid(value);
    }

    private boolean isValidUuid(UUID value) {
        try {
            UUID.fromString(value.toString());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }
}
