package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormResponseDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormResponseService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.utils.exception.DynamoExceptionHandler;

import lombok.RequiredArgsConstructor;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class FormResponseControllerTest {
    private final String formUniqueId = "123450900";
    private MockMvc mockMvc;
    @Mock
    private FormResponseService formResponseService;
    private ObjectMapper objectMapper;

    private static ObjectNode getJsonNodes() {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("name", "john doe");
        return jsonNode;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FormResponseController(formResponseService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new DynamoExceptionHandler()).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Save Form Response: Valid Request")
    public void saveFormResponse() throws Exception {
        // Given
        when(formResponseService.saveFormResponse(any(FormResponseDto.class))).thenReturn(getFormResponse());

        // When and Then
        mockMvc.perform(post("/api/form-responses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getFormResponseDto()))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.form.id").value(getForm().getId()));

        // Verify that the service method was called
        verify(formResponseService).saveFormResponse(any(FormResponseDto.class));
    }

    @Test
    @DisplayName("Retrieve Form Responses: Valid Request")
    public void retrieveFormResponsesValidRequest() throws Exception {
        // Given
        long formId = 1000L;
        MultiValueMap<String, String> searchParameters = new LinkedMultiValueMap<>();
        int pageNo = 0;
        int pageSize = 10;
        List<FormResponse> formResponseList = List.of(getFormResponse());
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        Page<FormResponse> expectedformResponsepage = new PageImpl<>(formResponseList, pageRequest, 1);
        when(formResponseService.retrieveFormResponses(eq(formId), eq(searchParameters), any(Pageable.class)))
                .thenReturn(expectedformResponsepage);

        // When and Then
        mockMvc.perform(get("/api/form-responses/{form-id}", formId).params(searchParameters))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.pageable").isMap())
                .andExpect(jsonPath("$.totalElements").value(expectedformResponsepage.getTotalElements()));

        // Verify that the service method was called
        verify(formResponseService).retrieveFormResponses(eq(formId), eq(searchParameters), any(Pageable.class));
    }

    @Test
    @DisplayName("Retrieve Form Response: Valid Request")
    public void testRetrieveFormResponse() throws Exception {
        // Given
        FormResponse formResponse = getFormResponse();

        // Mock the service method to return the sample FormResponse object
        when(formResponseService.retrieveFormResponse(formResponse.getId())).thenReturn(formResponse);

        // When / Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/form-responses").param("form-response-id",
                String.valueOf(formResponse.getId()))).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(formResponse.getId()));
    }

    @Test
    @DisplayName("Delete Form Response - Success")
    public void testDeletesFormResponse() throws Exception {
        // Given
        long formResponseId = 1L;

        // When
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/form-responses/{form-response-id}", formResponseId))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Verify that method is called
        verify(formResponseService, times(1)).deleteFormResponse(formResponseId);
    }

    private Form getForm() {
        return Form.builder().uniqueId(formUniqueId).id(1L).createdOn(Instant.now()).build();
    }

    private FormResponse getFormResponse() {
        return FormResponse.builder().id(10L).responseJson(getJsonNodes()).form(getForm()).build();
    }

    private FormResponseDto getFormResponseDto() {
        ObjectNode jsonNode = getJsonNodes();
        FormResponseDto formResponseDto = new FormResponseDto();
        formResponseDto.setFormUniqueId(formUniqueId);
        formResponseDto.setResponseJson(jsonNode);
        return formResponseDto;
    }

}