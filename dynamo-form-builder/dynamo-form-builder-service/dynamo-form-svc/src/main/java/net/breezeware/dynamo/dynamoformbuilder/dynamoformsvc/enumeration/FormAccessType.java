package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum FormAccessType {
    PUBLIC("public"), PRIVATE("private");

    private final String value;

    public static Optional<FormAccessType> retrieveFormAccessType(String value) {
        log.info("Entering retrieveFormAccessType(), value: {}", value);
        Optional<FormAccessType> optionalFormAccessType = Arrays.stream(values())
                .filter(formStatus -> formStatus.value.equalsIgnoreCase(value.toLowerCase())).findFirst();
        log.info("Leaving retrieveFormAccessType(), is FormAccessType available for value: {} = {}", value,
                optionalFormAccessType.isPresent());
        return optionalFormAccessType;
    }

}
