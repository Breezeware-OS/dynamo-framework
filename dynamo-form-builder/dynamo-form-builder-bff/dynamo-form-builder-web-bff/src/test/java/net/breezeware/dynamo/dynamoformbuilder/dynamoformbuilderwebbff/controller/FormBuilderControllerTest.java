package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormVersionDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormBuilderService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;
import net.breezeware.dynamo.utils.exception.DynamoExceptionHandler;

import lombok.RequiredArgsConstructor;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class FormBuilderControllerTest {

    private MockMvc mockMvc;
    @Mock
    private FormBuilderService formBuilderService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FormBuilderController(formBuilderService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new DynamoExceptionHandler()).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Publish Form: Valid Request")
    public void publishFormValidRequest() throws Exception {
        // Given
        JsonNode formJson = objectMapper.readTree("{\"Name\": \"Bharat kumar\"}");
        Form expectedForm = Form.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").status("Published")
                .createdOn(Instant.now()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(formBuilderService.publishForm(any(FormDto.class))).thenReturn(expectedForm);

        // When and Then
        mockMvc.perform(post("/api/forms/publish").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formJson))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedForm.getId()))
                .andExpect(jsonPath("$.status").value("Published"));

        // Verify that the service method was called
        verify(formBuilderService).publishForm(any(FormDto.class));
    }

    @Test
    @DisplayName("Draft Form: Valid Request")
    public void draftFormValidRequest() throws Exception {
        // Given
        JsonNode formJson = objectMapper.readTree("{\"Name\": \"Bharat kumar\"}");
        Form expectedForm = Form.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").status("Draft")
                .createdOn(Instant.now()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(formBuilderService.draftForm(any(FormDto.class))).thenReturn(expectedForm);

        // When and Then
        mockMvc.perform(post("/api/forms/draft").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(formJson))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedForm.getId())).andExpect(jsonPath("$.status").value("Draft"));

        // Verify that the service method was called
        verify(formBuilderService).draftForm(any(FormDto.class));
    }

    @Test
    @DisplayName("Retrieve Forms: Valid Request")
    public void retrieveFormsValidRequest() throws Exception {
        JsonNode formJson = objectMapper.readTree("{\"Name\": \"Bharat kumar\"}");
        // Given
        MultiValueMap<String, String> searchParameters = new LinkedMultiValueMap<>();
        int pageNo = 0;
        int pageSize = 10;
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        Page<Form> expectedFormPage = new PageImpl<>(List.of(buildForm()), pageRequest, 1);
        when(formBuilderService.retrieveForms(any(MultiValueMap.class), any(Pageable.class)))
                .thenReturn(expectedFormPage);

        // When and Then
        mockMvc.perform(get("/api/forms").params(searchParameters)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.content").isArray())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(expectedFormPage.getTotalElements()));

        // Verify that the service method was called
        verify(formBuilderService).retrieveForms(any(MultiValueMap.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Retrieve Form: Valid Request")
    public void retrieveFormValidRequest() throws Exception {
        // Given
        long formId = 1000L;
        FormDto formDto = buildFormDto();
        when(formBuilderService.retrieveForm(formId)).thenReturn(formDto);

        // When and Then
        mockMvc.perform(get("/api/forms/{form-id}", formId)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(formDto.getId()))
                .andExpect(jsonPath("$.name").value(formDto.getName()));

        // Verify that the service method was called
        verify(formBuilderService).retrieveForm(formId);
    }

    @Test
    @DisplayName("Retrieve Form Versions: Valid Request")
    public void retrieveFormVersionsValidRequest() throws Exception {
        // Given
        long formId = 1000L;
        MultiValueMap<String, String> searchParameters = new LinkedMultiValueMap<>();
        int pageNo = 0;
        int pageSize = 10;
        List<FormVersionDto> formVersionDtoList = List.of(new FormVersionDto("v1.0.0", Instant.now()));
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        Page<FormVersionDto> expectedFormVersionPage = new PageImpl<>(formVersionDtoList, pageRequest, 1);
        when(formBuilderService.retrieveFormVersions(eq(formId), eq(searchParameters), any(Pageable.class)))
                .thenReturn(expectedFormVersionPage);

        // When and Then
        mockMvc.perform(get("/api/forms/{form-id}/form-versions", formId).params(searchParameters))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.pageable").isMap())
                .andExpect(jsonPath("$.totalElements").value(expectedFormVersionPage.getTotalElements()));

        // Verify that the service method was called
        verify(formBuilderService).retrieveFormVersions(eq(formId), any(MultiValueMap.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Archive Form: Valid Request")
    public void archiveFormValidRequest() throws Exception {
        // Given
        long formId = 1000L;

        // Mock the service method to return a successful response
        doNothing().when(formBuilderService).archiveForm(formId);

        // When and Then
        mockMvc.perform(delete("/api/forms/{form-id}", formId)).andExpect(status().isOk());

        // Verify that the service method was called
        verify(formBuilderService).archiveForm(formId);
    }

    @Test
    @DisplayName("Form status count: Valid Request")
    public void retrieveFormStatusCountTest() throws Exception {
        // Given
        Map<String, Long> statusCountMap = new HashMap<>();
        statusCountMap.put("published", 10L);
        statusCountMap.put("draft", 5L);
        statusCountMap.put("archived", 3L);
        when(formBuilderService.retrieveFormStatusCount()).thenReturn(statusCountMap);

        // Perform the GET request
        mockMvc.perform(get("/api/forms/status-count").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.published").value(10)).andExpect(jsonPath("$.draft").value(5))
                .andExpect(jsonPath("$.archived").value(3));

        // Verify that retrieveFormStatusCount method in formBuilderService was called
        verify(formBuilderService, times(1)).retrieveFormStatusCount();
    }

    @Test
    @DisplayName("Retrieve Form By unique Id: Valid Request")
    public void retrieveFormByUniqueIdValidRequest() throws Exception {
        // Given
        String formId = "randomId";
        FormDto formDto = buildFormDto();
        when(formBuilderService.retrieveForm(formId)).thenReturn(formDto);

        // When and Then
        mockMvc.perform(get("/api/forms/form").queryParam("unique-id", formId)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(formDto.getId()))
                .andExpect(jsonPath("$.name").value(formDto.getName()));

        // Verify that the service method was called
        verify(formBuilderService).retrieveForm(formId);
    }

    public Form buildForm() {
        return Form.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0").status("Published")
                .createdOn(Instant.now()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

    }

    public FormDto buildFormDto() throws JsonProcessingException {
        return FormDto.builder().id(1000L).name("Employee Information")
                .description("A form for collecting employee information.").version("v1.0.0")
                .formJson(objectMapper.readTree("{\"Name\": \"Akash kumar\"}")).build();

    }

    public FormVersion buildFormVersion(Form form, JsonNode jsonNode) {
        return FormVersion.builder().id(1000L).version("v2.0.0").status("Draft").formJson(jsonNode).form(form)
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
    }

}
