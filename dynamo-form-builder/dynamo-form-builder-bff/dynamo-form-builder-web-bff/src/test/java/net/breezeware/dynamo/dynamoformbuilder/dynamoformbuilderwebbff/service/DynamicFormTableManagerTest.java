package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dynamic Form TableManager Test")
class DynamicFormTableManagerTest {

    public static String FORM_JSON = """
            {
              "name": "Sample",
              "description": "Description",
              "components": [
                {
                  "label": "Fullname",
                  "firstNameLabel": "First Name",
                  "lastNameLabel": "Last Name",
                  "firstNamePlaceholder": "First Name",
                  "lastNamePlaceholder": "Last Name",
                  "required": false,
                  "size": "lg",
                  "key": "fullname_74d323e6",
                  "type": "fullname",
                  "fieldName": "Fullname",
                  "value": "Kishore Chinnaswamy"
                },
                {
                  "label": "Text",
                  "placeholder": "Text",
                  "required": false,
                  "size": "lg",
                  "key": "text_input_b1dd028b",
                  "type": "textField",
                  "fieldName": "Text Field",
                  "value": "Sample Text"
                },
                  {
                  "label": "Text",
                  "placeholder": "Text",
                  "required": false,
                  "size": "lg",
                  "key": "text_input_b1dd0288",
                  "type": "textField",
                  "fieldName": "Text Field",
                  "value": "Sample Text"
                },
                {
                  "fieldName": "Date",
                  "label": "Date",
                  "placeholder": "Date",
                  "required": false,
                  "size": "lg",
                  "key": "date_3fdbe6f4",
                  "type": "datetime",
                  "value": "2024-04-12"
                },
                {
                  "fieldName": "Text Area",
                  "label": "Text  Area",
                  "placeholder": "Text Area",
                  "required": false,
                  "size": "lg",
                  "key": "text_area_7ed2f761",
                  "type": "textarea",
                  "value": "text area"
                },
                {
                  "fieldName": "Number",
                  "label": "Number",
                  "required": false,
                  "size": "lg",
                  "key": "number_b4592e6e",
                  "type": "number",
                  "value": "98"
                },
                {
                  "fieldName": "Phone Number",
                  "label": "Phone Number",
                  "required": false,
                  "size": "lg",
                  "key": "phoneNumber_ef6f7de6",
                  "type": "phoneNumber",
                  "value": "8521479630"
                },
                {
                  "fieldName": "Email",
                  "label": "Email",
                  "required": false,
                  "size": "lg",
                  "key": "email_35ea42d5",
                  "type": "email",
                  "value": "kishore@breezeware.net",
                  "errorMessage": "Please enter a valid email address.",
                  "placeholder": "Email"
                },
                {
                  "fieldName": "Address",
                  "label": {
                    "addressLine1": "Adress Line1",
                    "addressLine2": "Address Line2",
                    "city": "City",
                    "postalCode": "Postal Code",
                    "state": "State"
                  },
                  "required": false,
                  "size": "lg",
                  "key": "address_4a0582f5",
                  "type": "address",
                  "value": {
                    "addressline1": "18/9",
                    "addressline2": "pudur",
                    "city": "vadavalli",
                    "postalCode": "tamil nadu",
                    "state": "12345"
                  },
                  "placeholder": {
                    "addressLine1Placeholder": "Address Line 1",
                    "addressLine2Placeholder": "Address Line 2",
                    "cityPlaceholder": "City",
                    "statePlaceholder": "State",
                    "postalCodePlaceholder": "Potsal code"
                  }
                },
                {
                  "fieldName": "Select",
                  "label": "Select",
                  "placeholder": "Select",
                  "required": false,
                  "size": "lg",
                  "key": "select_ed1e165d",
                  "type": "select",
                  "value": "Value 2",
                  "options": [
                    {
                      "key": "option_3615bbd7",
                      "label": "Label",
                      "value": "Value"
                    },
                    {
                      "id": "option_9fce01ac",
                      "label": "Label 2",
                      "value": "Value 2"
                    }
                  ]
                },
                {
                  "fieldName": "Radio",
                  "label": "Radio",
                  "required": false,
                  "size": "lg",
                  "key": "radio_439066a1",
                  "type": "radio",
                  "value": "Value 2",
                  "options": [
                    {
                      "key": "option_be83d2ab",
                      "label": "Label",
                      "value": "Value"
                    },
                    {
                      "id": "option_dd80c587",
                      "label": "Label 2",
                      "value": "Value 2"
                    }
                  ]
                },
                {
                  "fieldName": "Checkbox",
                  "label": "Checkbox",
                  "required": false,
                  "size": "lg",
                  "key": "checkbox_c6fcab64",
                  "type": "checkbox",
                  "checked": false,
                  "disabled": false,
                  "value": true
                },
                {
                  "fieldName": "Checkbox Group",
                  "label": "Checkbox Group",
                  "required": false,
                  "size": "lg",
                  "key": "checkboxGroup_049b1803",
                  "type": "checkboxGroup",
                  "value": [
                    "Value 2"
                  ],
                  "options": [
                    {
                      "key": "checkboxGroupOption_83d960f1",
                      "label": "Label",
                      "value": "Label"
                    },
                    {
                      "id": "option_01564fd9",
                      "label": "Label 2",
                      "value": "Value 2"
                    }
                  ]
                },
                {
                  "fieldName": "Multi Select Dropdown",
                  "label": "Multi Select",
                  "required": false,
                  "size": "lg",
                  "key": "multiSelect_bb34e64a",
                  "type": "multiSelect",
                  "value": [
                    "Value",
                    "Value 2"
                  ],
                  "options": [
                    {
                      "key": "option_7c1c7a5d",
                      "label": "Label",
                      "value": "Value"
                    },
                    {
                      "id": "option_7cd180f7",
                      "label": "Label 2",
                      "value": "Value 2"
                    }
                  ]
                }
              ]
            }
            """;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DataSource dataSource;

    @Mock
    private DatabaseMetaData metaData;

    @InjectMocks
    private DynamicFormTableManager dynamicFormTableManager;

    @Test
    void createOrUpdateFormSubmissionTable_CreateTable_Success() throws JsonProcessingException {
        String tableName = "test_table";
        String schemaName = "test_table";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode formContent = mapper.readTree(FORM_JSON);

        // Mocking behavior for isTableCreated method to return false
        when(dynamicFormTableManager.isTableCreated(tableName)).thenReturn(false);

        // Verify that createTable method is called when table doesn't exist
        dynamicFormTableManager.createOrUpdateFormSubmissionTable(formContent, schemaName, tableName);

        // Verify that createTable method is called only once
        verify(jdbcTemplate, times(1)).execute(any(String.class));
    }

    @Test
    void isTableCreated_TableExists_ReturnsTrue() {
        String tableName = "test_table";
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(tableName))).thenReturn(true);

        assertTrue(dynamicFormTableManager.isTableCreated(tableName));
    }

    @Test
    void isTableCreated_TableDoesNotExist_ReturnsFalse() {
        String tableName = "test_table";
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(tableName))).thenReturn(false);

        assertFalse(dynamicFormTableManager.isTableCreated(tableName));
    }

}
