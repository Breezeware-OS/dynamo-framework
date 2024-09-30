package net.breezeware.dynamo.csv.config;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom mapping strategy based on {@link ColumnPositionMappingStrategy} for
 * OpenCSV.
 * @param <T> Type parameter of the object to be mapped.
 */
@Slf4j
public class CustomMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {
    @Override
    public String[] generateHeader(T bean) {
        log.debug("Entering generateHeader(), bean = {}", bean);
        Field[] fields = bean.getClass().getDeclaredFields();
        String[] columnsOrHeader = Arrays.stream(fields)
                // retrieves only the field or column name
                .map(this::getColumnName)
                // converts the stream into String[]
                .toArray(String[]::new);
        log.debug("Leaving generateColumnsOrHeader() for class = {} with columnsOrHeader = {}",
                this.getClass().getName(), columnsOrHeader);
        return columnsOrHeader;
    }

    /**
     * Gets column or header name from the field.<br>
     * Gets column name from {@link CsvBindByName} annotation if present or returns
     * the actual field name.
     * @param  field {@link Field} from which the column name will be processed.
     * @return       column or header name.
     */
    private String getColumnName(Field field) {
        log.debug("Entering getColumnName(), field = {}", field);
        String columnName;
        if (field.isAnnotationPresent(CsvBindByName.class)) {
            columnName = field.getAnnotation(CsvBindByName.class).column();
        } else {
            columnName = field.getName();
        }

        log.debug("Leaving getColumnName(), columnName = {}", columnName);
        return columnName;
    }
}
