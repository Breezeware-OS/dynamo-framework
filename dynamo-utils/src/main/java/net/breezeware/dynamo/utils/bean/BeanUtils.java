package net.breezeware.dynamo.utils.bean;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Utility class for working with Java beans.
 */
public class BeanUtils {

    /**
     * Retrieves a list of maps representing beans with only the specified fields.
     * @param  beans           The list of beans to extract fields from.
     * @param  requestedFields An array of field names to include in the result
     *                         maps.
     * @param  <T>             The type of beans in the list.
     * @return                 A list of maps, where each map represents a bean with
     *                         only the specified fields.
     */
    public static <T> List<Map<Object, Object>> getBeansWithFields(List<T> beans, String[] requestedFields) {
        List<Map<Object, Object>> beansWithSpecificFields = new ArrayList<>(beans.size());
        for (T bean : beans) {
            beansWithSpecificFields.add(getBeanWithFields(bean, requestedFields));
        }

        return beansWithSpecificFields;
    }

    /**
     * Retrieves a map representing a bean with only the specified fields.
     * @param  bean            The bean to extract fields from.
     * @param  requestedFields An array of field names to include in the result map.
     * @param  <T>             The type of the bean.
     * @return                 A map representing the bean with only the specified
     *                         fields.
     */
    public static <T> Map<Object, Object> getBeanWithFields(T bean, String[] requestedFields) {
        final BeanWrapper src = new BeanWrapperImpl(bean);
        Map<Object, Object> beanWithSpecificFields = new HashMap<>();
        for (String field : requestedFields) {
            PropertyDescriptor propertyDescriptor = src.getPropertyDescriptor(field);
            beanWithSpecificFields.put(propertyDescriptor.getDisplayName(), src.getPropertyValue(field));
        }

        return beanWithSpecificFields;
    }

    public static <T> boolean hasField(Class<T> type, String requestedField) {
        PropertyDescriptor propertyDescriptor =
                org.springframework.beans.BeanUtils.getPropertyDescriptor(type, requestedField);
        return Objects.nonNull(propertyDescriptor) && !propertyDescriptor.getDisplayName().isBlank();
    }
}
