package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.utils.jsonconverter;

import java.io.IOException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JPA attribute converter for mapping JSON data between a JSONNode object and a
 * String representation when storing JSON in a database.
 */
@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<JsonNode, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Converts a JSONNode object to its corresponding JSON string representation.
     * @param  node                     the JSONNode object to be converted.
     * @return                          a JSON string representing the input
     *                                  JSONNode.
     * @throws IllegalArgumentException if an error occurs during the conversion.
     */
    @Override
    public String convertToDatabaseColumn(JsonNode node) {
        try {
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JsonNode to JSON string", e);
        }

    }

    /**
     * Converts a JSON string to a JSONNode object.
     * @param  data                     the JSON string to be converted.
     * @return                          a JSONNode object representing the input
     *                                  JSON string.
     * @throws IllegalArgumentException if an error occurs during the conversion.
     */
    @Override
    public JsonNode convertToEntityAttribute(String data) {
        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON string to JsonNode", e);
        }

    }
}
