package net.breezeware.dynamo.csv.entity;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class to handle common CSV file related operations. Configured
 * to generate column or header.
 */
@EqualsAndHashCode
@Slf4j
@Data
public abstract class BaseCsvFile {

    /**
     * Generates Columns or header from the class's fields.
     * @return array of columns/header name.
     */
    public String[] generateColumnsOrHeader() {
        log.debug("Entering generateColumnsOrHeader() for class = {}", this.getClass().getName());
        Field[] fields = this.getClass().getDeclaredFields();
        String[] columnsOrHeader = Arrays.stream(fields)
                // retrieves only the field name
                .map(this::getFieldName)
                // converts the stream into String[]
                .toArray(String[]::new);
        log.debug("Leaving generateColumnsOrHeader() for class = {} with columnsOrHeader = {}",
                this.getClass().getName(), columnsOrHeader);
        return columnsOrHeader;
    }

    /**
     * Gets name from the field.<br>
     * Gets name from {@link CsvBindByName} annotation if present or else returns
     * the actual field name.
     * @param  field {@link Field} from which the name will be processed.
     * @return       name.
     */
    private String getFieldName(Field field) {
        log.debug("Entering getName(), field = {}", field);
        String name;
        if (field.isAnnotationPresent(CsvBindByName.class)) {
            log.debug("CsvColumn annotation present. Retrieving name from the annotation");
            // Gets name from the CsvBindByName annotation
            name = field.getAnnotation(CsvBindByName.class).column();
            log.debug("Retrieved name from the annotation = {}", name);
        } else {
            log.debug("No CsvBindByName present. Retrieving name from the field");
            // Gets name from the field itself
            name = field.getName();
            log.debug("Retrieved name from the field = {}", name);
        }

        log.debug("Leaving getName(), name = {}", name);
        return name;
    }
}
