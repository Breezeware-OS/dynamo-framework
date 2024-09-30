package net.breezeware.dynamo.utils.exception;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling validation exceptions.
 */
@Slf4j
public class ValidationExceptionUtils {

    /**
     * Handles the constraint violations by throwing a DynamoException if any
     * violations are found.
     * @param  fieldViolations A Set of constraint violations.
     * @throws DynamoException if there are field violations.
     */
    public static <T> void handleException(Set<ConstraintViolation<T>> fieldViolations) throws DynamoException {
        log.info("Entering handleException()");
        if (!fieldViolations.isEmpty()) {
            log.info("# of field violations: {}", fieldViolations.size());
            List<String> messages = fieldViolations.stream().map(ConstraintViolation::getMessage).toList();
            log.info("Throwing field errors as DynamoException");
            throw new DynamoException(messages, HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving handleException(), no field errors!");

    }
}
