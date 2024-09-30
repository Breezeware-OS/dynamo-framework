package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonvalidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A custom constraint validator for validating JSONNode objects annotated with
 * the @ValidJsonNode constraint. This validator checks if a JSONNode is not
 * null and not representing a null value.
 */
public class JsonNodeValidator implements ConstraintValidator<ValidJsonNode, JsonNode> {

    /**
     * Validates a JSONNode object to ensure it is not null.
     * @param  jsonNode the JSONNode object to be validated.
     * @param  context  the constraint validation context.
     * @return          true if the JSONNode is valid, false otherwise.
     */
    @Override
    public boolean isValid(JsonNode jsonNode, ConstraintValidatorContext context) {
        return jsonNode != null;
    }
}
