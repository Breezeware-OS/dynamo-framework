package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicFormTableManager {

    public static final String COLUMN_LABEL = "COLUMN_NAME";
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates or updates a table for storing form submissions based on the provided
     * form content, schema name, and table name.
     * @param formContent The JSON content of the form.
     * @param schemaName  The name of the schema.
     * @param tableName   The name of the table.
     */
    public void createOrUpdateFormSubmissionTable(JsonNode formContent, String schemaName, String tableName) {
        log.info("Entering createOrUpdateFormSubmissionTable() schemaName= {} tableName= {}", schemaName, tableName);
        // Check if the table already exists
        if (!isTableCreated(tableName)) {
            createTable(formContent, schemaName, tableName);
        } else {
            updateTable(formContent, schemaName, tableName);
        }

        log.info("Leaving createOrUpdateFormSubmissionTable()");
    }

    /**
     * Creates a table for storing form submissions based on the provided form
     * content, schema name, and table name.
     * @param formContent The JSON content of the form.
     * @param schemaName  The name of the schema.
     * @param tableName   The name of the table.
     */
    public void createTable(JsonNode formContent, String schemaName, String tableName) {
        log.info("Entering createTable() tableName= {}", tableName);
        validateRequest(formContent, schemaName, tableName);

        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(schemaName).append(".")
                .append(tableName).append("(");
        createTableQuery.append("id serial PRIMARY KEY, ");
        Set<String> existingColumnNames = new HashSet<>();
        for (JsonNode component : formContent.get("components")) {
            String label = component.get("label").isTextual() ? component.get("label").asText()
                    : component.get("fieldName").asText();
            String type = component.get("type").asText();
            String columnType = getColumnType(type);
            String columnName = label;
            int suffix = 1;
            while (existingColumnNames.contains(columnName)) {
                columnName = label + "_" + suffix;
                suffix++;
            }

            existingColumnNames.add(columnName);
            createTableQuery.append("\"").append(columnName).append("\"").append(" ").append(columnType).append(",");
        }

        createTableQuery.append("\"submission_date\" timestamp with time zone DEFAULT current_timestamp, ");
        createTableQuery.append("\"form_version\" varchar(50), ");

        createTableQuery.append("form_id int8, FOREIGN KEY (form_id) REFERENCES dynamo.form(id)");
        createTableQuery.append(");");
        log.info("Leaving Create Table Query: {}", createTableQuery);
        jdbcTemplate.execute(createTableQuery.toString());
    }

    /**
     * Updates a table for storing form submissions based on the provided form
     * content, schema name, and table name.
     * @param formContent The JSON content of the form.
     * @param schemaName  The name of the schema.
     * @param tableName   The name of the table.
     */
    public void updateTable(JsonNode formContent, String schemaName, String tableName) {
        log.info("Entering updateTable() tableName= {}", tableName);
        validateRequest(formContent, schemaName, tableName);
        Map<String, String> expectedColumns = extractExpectedColumns(formContent);
        Set<String> existingColumns = getExistingColumns(schemaName, tableName);
        for (Map.Entry<String, String> entry : expectedColumns.entrySet()) {
            String column = entry.getKey();
            String columnType = entry.getValue();
            if (!existingColumns.contains(column)) {
                addColumnToTable(schemaName, tableName, column, columnType);
            }

        }

        log.info("Leaving updateTable()");
    }

    /**
     * Validates the request parameters for creating or updating a table.
     * @param  formContent     The JSON content of the form.
     * @param  schemaName      The name of the schema.
     * @param  tableName       The name of the table.
     * @throws DynamoException If the form content, schema name, or table name is
     *                         invalid.
     */
    private void validateRequest(JsonNode formContent, String schemaName, String tableName) {
        log.info("Entering validateRequest()");
        if (formContent == null) {
            throw new DynamoException("Form Content must not be null.", HttpStatus.BAD_REQUEST);
        }

        if (schemaName == null || schemaName.isEmpty()) {
            throw new DynamoException("""
                    Schema Name must not be null. If you are creating a table, please provide a schema name. \
                    If no schema name is specified, use 'public' as the schema name.\
                    """, HttpStatus.BAD_REQUEST);

        }

        if (tableName == null || tableName.isEmpty()) {
            throw new DynamoException("Table Name must not be null.", HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validateRequest()");

    }

    /**
     * Checks if a table with the specified name already exists in the database.
     * @param  tableName The name of the table.
     * @return           True if the table exists; otherwise, false.
     */
    public boolean isTableCreated(String tableName) {
        log.info("Entering isTableCreated() tableName= {}", tableName);
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)";
        boolean tableExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, tableName));
        log.info("Leaving isTableCreated() #tableExists = {}", tableExists);
        return tableExists;
    }

    /**
     * Retrieves the column type based on the form component type.
     * @param  type The type of the form component.
     * @return      The corresponding column type in the database.
     */

    private String getColumnType(String type) {
        log.info("Entering getColumnType() type = {}", type);
        String columnType = switch (type) {
            case "number" -> "int8";
            case "datetime", "time" -> "timestamp";
            default -> "varchar(255)";
        };
        log.info("Leaving getColumnType() columnType= {}", columnType);
        return columnType;
    }

    /**
     * Extracts the expected columns from the JSON representation of the form.
     * @param  jsonNode The JSON representation of the form.
     * @return          A map of column names and their corresponding types.
     */
    private Map<String, String> extractExpectedColumns(JsonNode jsonNode) {
        log.info("Entering extractExpectedColumns()");
        Map<String, String> columns = new HashMap<>();
        JsonNode components = jsonNode.get("components");
        if (components != null && components.isArray()) {
            for (JsonNode component : components) {
                JsonNode labelNode = component.get("label");
                JsonNode typeNode = component.get("type");
                JsonNode keyNode = component.get("key");

                if (labelNode != null && keyNode != null && typeNode != null) {
                    String columnName =
                            labelNode.isTextual() ? labelNode.asText() : component.get("fieldName").asText();
                    String columnType = getColumnType(typeNode.asText());
                    columns.put(columnName, columnType);
                }

            }

        }

        log.info("Leaving extractExpectedColumns()");
        return columns;
    }

    /**
     * Retrieves existing columns from the specified table in the database.
     * @param  schemaName The name of the schema.
     * @param  tableName  The name of the table.
     * @return            A set of existing column names.
     */
    public Set<String> getExistingColumns(String schemaName, String tableName) {
        log.info("Entering getExistingColumns() tableName ={}", tableName);
        try {
            Set<String> existingColumns = new HashSet<>();
            DatabaseMetaData metaData =
                    Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getMetaData();
            ResultSet resultSet = metaData.getColumns(null, schemaName, tableName, null);
            while (resultSet.next()) {
                existingColumns.add(resultSet.getString(COLUMN_LABEL));
            }

            log.info("Leaving getExistingColumns()");
            return existingColumns;
        } catch (SQLException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Adds a new column to the specified table in the database.
     * @param schemaName The name of the schema.
     * @param tableName  The name of the table.
     * @param columnName The name of the column to add.
     * @param columnType The data type of the column.
     */
    private void addColumnToTable(String schemaName, String tableName, String columnName, String columnType) {
        log.info("Entering addColumnToTable() schemaName= {} ,tableName= {} ,columnName= {} ,columnType= {}",
                schemaName, tableName, columnName, columnType);
        StringBuilder addColumnQuery = new StringBuilder("ALTER TABLE ").append(schemaName).append(".")
                .append(tableName).append(" ADD COLUMN ").append("\"").append(columnName).append("\"").append(" ")
                .append(columnType);

        log.info("Leaving addColumnToTable() addColumnQuery= {}", addColumnQuery);
        jdbcTemplate.execute(addColumnQuery.toString());
    }

    /**
     * Inserts form data into the specified table in the database.
     * @param formContent The JSON content of the form.
     * @param schemaName  The name of the schema.
     * @param tableName   The name of the table.
     * @param formId      The ID of the form.
     * @param formVersion The version of the form.
     */
    public void insertDataIntoTable(JsonNode formContent, String schemaName, String tableName, long formId,
            String formVersion) {
        log.info("Entering insertDataIntoTable() schemaName= {} ,tableName= {} ,FormId= {} ,formVersion= {} ",
                schemaName, tableName, formId, formVersion);
        try {
            Map<String, Object> columnValues = extractColumnValues(formContent);
            // Append columns
            StringBuilder insertIntoQuery =
                    new StringBuilder("INSERT INTO ").append(schemaName).append(".").append(tableName).append(" (");
            for (String columnName : columnValues.keySet()) {
                if (!columnName.isEmpty()) {
                    insertIntoQuery.append("\"").append(columnName).append("\",");
                }

            }

            insertIntoQuery.append("\"form_id\",\"form_version\") VALUES (");

            // Append values
            for (Object value : columnValues.values()) {
                if (value instanceof String) {
                    insertIntoQuery.append("'").append(value).append("',");
                } else {
                    insertIntoQuery.append(value).append(",");
                }

            }

            insertIntoQuery.append(formId).append(",").append("'").append(formVersion).append("'").append(")");
            log.info("Insert Into Query= {}", insertIntoQuery);
            jdbcTemplate.execute(insertIntoQuery.toString());
        } catch (Exception e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Extracts column values from the JSON representation of the form.
     * @param  formContent The JSON content of the form.
     * @return             A map of column names and their corresponding values.
     */
    private Map<String, Object> extractColumnValues(JsonNode formContent) {
        log.info("Entering extractColumnValues()");
        Map<String, Object> columnValues = new LinkedHashMap<>();
        JsonNode components = formContent.get("components");

        if (components != null && components.isArray()) {
            for (JsonNode component : components) {
                JsonNode labelNode = component.get("label");
                JsonNode keyNode = component.get("key");
                JsonNode valueNode = component.get("value");
                JsonNode fieldNameNode = component.get("fieldName");

                if (keyNode != null && labelNode != null && valueNode != null && fieldNameNode != null) {
                    String key = labelNode.isTextual() ? labelNode.asText() : fieldNameNode.asText();
                    int suffix = 1;
                    while (columnValues.containsKey(key)) {
                        key = labelNode.asText() + "_" + suffix;
                        suffix++;
                    }

                    Object value = extractValueObject(valueNode);
                    if (valueNode.isObject()) {
                        try {
                            value = objectMapper.writeValueAsString(valueNode);
                        } catch (JsonProcessingException e) {
                            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
                        }

                    } else if (valueNode.isArray()) {
                        StringBuilder arrayValues = new StringBuilder();
                        for (JsonNode element : valueNode) {
                            if (element.isTextual()) {
                                arrayValues.append(element.textValue()).append(",");
                            } else {
                                arrayValues.append(element).append(",");
                            }

                        }

                        // Remove the trailing comma
                        if (!arrayValues.isEmpty()) {
                            arrayValues.setLength(arrayValues.length() - 1);
                        }

                        value = arrayValues.toString();
                    }

                    columnValues.putIfAbsent(key, value);
                }

            }

        }

        log.info("Leaving extractColumnValues() columnValues #size() {}", columnValues.size());
        return columnValues;
    }

    /**
     * Extracts the value object from the JSON node.
     * @param  valueNode The JSON node containing the value.
     * @return           The corresponding value object.
     */
    private Object extractValueObject(JsonNode valueNode) {
        log.info("Entering extractValueObject()");
        Object objectType;
        switch (valueNode.getNodeType()) {
            case BOOLEAN:
                objectType = valueNode.asBoolean();
                break;
            case NUMBER:
                if (valueNode.isInt()) {
                    objectType = valueNode.asInt();
                } else if (valueNode.isLong()) {
                    objectType = valueNode.asLong();
                } else {
                    objectType = valueNode.asDouble();
                }
                break;
            case OBJECT, ARRAY:
                objectType = valueNode;
                break;
            default:
                objectType = valueNode.asText();
                break;
        }

        log.info("Leaving extractValueObject() objectType = {} ", objectType);
        return objectType;
    }

    /**
     * Retrieves paginated data from the specified table in the database based on
     * search terms and sorting criteria.
     * @param  schemaName  The name of the schema.
     * @param  tableName   The name of the table.
     * @param  searchTerms The search terms.
     * @param  sortBy      The field to sort by.
     * @param  sortOrder   The sort order (ASC or DESC).
     * @param  pageNumber  The page number.
     * @param  pageSize    The page size.
     * @return             A list of maps containing the retrieved data.
     */
    public List<Map<String, Object>> retrieveDataFromTable(String schemaName, String tableName,
            Map<String, String> searchTerms, String sortBy, String sortOrder, int pageNumber, int pageSize) {
        log.info("Entering retrieveDataFromTable()");
        // Calculate offset for pagination
        int offset = pageNumber * pageSize;
        if (offset <= 0) {
            offset = 0;
        }

        // Build the SQL query dynamically based on the parameters
        StringBuilder selectQuery =
                new StringBuilder("SELECT * FROM ").append(schemaName).append(".").append(tableName);

        // Add search condition if searchTerm is provided
        // Check if searchTerms map is not null and not empty
        appendSearchTerms(searchTerms, selectQuery);

        // Add sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            selectQuery.append(" ORDER BY ").append(sortBy);
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                selectQuery.append(" DESC");
            } else {
                selectQuery.append(" ASC");
            }

        }

        // Add pagination
        selectQuery.append(" LIMIT ").append(pageSize).append(" OFFSET ").append(offset);
        log.info("Select Query = {}", selectQuery);

        // Execute the query and map the result set to a list of maps
        List<Map<String, Object>> resultMaps = jdbcTemplate.queryForList(selectQuery.toString());

        log.info("Leaving retrieveDataFromTable()");
        return resultMaps;
    }

    /**
     * Retrieves the total number of elements from the specified table in the
     * database based on search terms.
     * @param  schemaName  The name of the schema.
     * @param  tableName   The name of the table.
     * @param  searchTerms The search terms.
     * @return             The total number of elements.
     */
    public int retrieveTotalElements(String schemaName, String tableName, Map<String, String> searchTerms) {
        log.info("Entering retrieveTotalElements()");

        StringBuilder countQuery =
                new StringBuilder("SELECT COUNT(*) FROM ").append(schemaName).append(".").append(tableName);

        // Check if searchTerms map is not null and not empty
        appendSearchTerms(searchTerms, countQuery);
        // Execute the count query
        Integer elementsCount = jdbcTemplate.queryForObject(countQuery.toString(), Integer.class);
        log.info("Leaving retrieveTotalElements() elementsCount= {}", elementsCount);
        return elementsCount != null ? elementsCount : 0;

    }

    private void appendSearchTerms(Map<String, String> searchTerms, StringBuilder queryString) {
        log.info("Entering appendSearchTerms()");
        if (searchTerms != null && !searchTerms.isEmpty()) {
            queryString.append(" WHERE ");

            boolean isFirstField = true;

            for (Map.Entry<String, String> entry : searchTerms.entrySet()) {
                String fieldName = entry.getKey();
                String searchTerm = entry.getValue();

                // Add AND keyword for multiple conditions
                if (!isFirstField) {
                    queryString.append(" AND ");
                }

                // Check if the field is an integer or date
                if (isInteger(searchTerm)) {
                    queryString.append("\"").append(fieldName).append("\" = ").append(searchTerm);
                } else if (isDate(searchTerm)) {
                    queryString.append("DATE_TRUNC('day', \"").append(fieldName).append("\")::date = DATE('")
                            .append(searchTerm).append("')");
                } else {
                    queryString.append("\"").append(fieldName).append("\" LIKE '%").append(searchTerm).append("%'");
                }

                isFirstField = false;
            }

        }

        log.info("Leaving appendSearchTerms()");
    }

    /**
     * Checks if the given string value represents an integer.
     * @param  value The string value to check.
     * @return       True if the value represents an integer; otherwise, false.
     */
    private boolean isInteger(String value) {
        log.info("Entering isInteger() value = {} ", value);
        try {
            Integer.parseInt(value);
            log.info("Leaving isInteger()");
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    /**
     * Checks if the given string value represents a date in the format
     * "yyyy-MM-dd".
     * @param  value The string value to check.
     * @return       True if the value represents a valid date; otherwise, false.
     */
    public boolean isDate(String value) {
        log.info("Entering isDate() value = {} ", value);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            // Parse the string as a date
            Date date = dateFormat.parse(value);
            log.info("Leaving isDate() ");
            log.info("Leaving isDate() date = {}", date);
            return true;
        } catch (ParseException e) {
            return false;
        }

    }

}
