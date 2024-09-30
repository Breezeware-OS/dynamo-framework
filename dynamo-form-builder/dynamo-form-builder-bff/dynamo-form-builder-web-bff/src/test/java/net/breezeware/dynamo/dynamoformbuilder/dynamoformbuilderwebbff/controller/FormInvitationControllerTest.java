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

import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto.FormInvitationDto;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.service.FormInvitationService;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.utils.exception.DynamoExceptionHandler;

import lombok.RequiredArgsConstructor;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class FormInvitationControllerTest {
    private final String formUniqueId = "123450900";
    private MockMvc mockMvc;
    @Mock
    private FormInvitationService formInvitationService;
    private ObjectMapper objectMapper;

    private static ObjectNode getJsonNodes() {
        ObjectNode jsonNode = new ObjectNode(JsonNodeFactory.instance);
        jsonNode.put("name", "john doe");
        return jsonNode;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FormInvitationController(formInvitationService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new DynamoExceptionHandler()).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Save Form Invitation: Valid Request")
    public void saveFormResponse() throws Exception {
        // When
        mockMvc.perform(post("/api/form-invitations").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getFormInvitationDto()))).andExpect(status().isOk());

        // Then
        verify(formInvitationService).saveFormInvitation(any(FormInvitationDto.class));
    }

    @Test
    @DisplayName("Retrieve Form Invitation: Valid Request")
    public void retrieveFormResponsesValidRequest() throws Exception {
        // Given
        long formId = 1000L;
        MultiValueMap<String, String> searchParameters = new LinkedMultiValueMap<>();
        int pageNo = 0;
        int pageSize = 10;
        List<FormInvitation> formInvitations = List.of(getFormInvitation());
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        Page<FormInvitation> formInvitationPage = new PageImpl<>(formInvitations, pageRequest, 1);
        when(formInvitationService.retrieveFormInvitations(eq(formId), eq(searchParameters), any(Pageable.class)))
                .thenReturn(formInvitationPage);

        // When and Then
        mockMvc.perform(get("/api/form-invitations/{form-id}", formId).params(searchParameters))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.pageable").isMap())
                .andExpect(jsonPath("$.totalElements").value(formInvitationPage.getTotalElements()));

        // Verify that the service method was called
        verify(formInvitationService).retrieveFormInvitations(eq(formId), eq(searchParameters), any(Pageable.class));
    }

    @Test
    @DisplayName("Delete Form Response - Success")
    public void testDeletesFormResponse() throws Exception {
        // Given
        long formInvitationId = 1L;

        // When
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/form-invitations/{form-invitation-id}", formInvitationId))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Verify that method is called
        verify(formInvitationService, times(1)).deleteFormInvitation(formInvitationId);
    }

    private Form getForm() {
        return Form.builder().id(1000L).uniqueId(formUniqueId).build();
    }

    private FormInvitation getFormInvitation() {
        return FormInvitation.builder().form(getForm()).id(1L).email("joe@gamil.com").build();
    }

    private FormInvitationDto getFormInvitationDto() {
        FormInvitationDto formInvitationDto = new FormInvitationDto();
        formInvitationDto.setFormUniqueId("validFormUniqueId");
        formInvitationDto.setEmailList(List.of("user1@example.com", "user2@example.com"));
        return formInvitationDto;
    }

}