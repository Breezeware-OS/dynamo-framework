package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao.FormVersionRepository;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("FormVersion Service Unit Test")
@ExtendWith(MockitoExtension.class)
class FormVersionServiceTest {

    @Mock
    FormVersionRepository formVersionRepository;

    @InjectMocks
    FormVersionService formVersionService;

    @Test
    @DisplayName("Retrieve FormVersion by Valid Form and Status")
    void retrieveFormVersionByValidFormAndStatus() {
        log.info("Entering retrieveFormVersionByValidFormAndStatus()");

        // Given
        Form form = new Form();
        String status = "active";
        FormVersion expectedFormVersion = new FormVersion();

        // Mock
        when(formVersionRepository.findByFormAndStatus(form, status)).thenReturn(Optional.of(expectedFormVersion));

        // When
        Optional<FormVersion> retrievedFormVersion = formVersionService.retrieveFormVersion(form, status);

        // Then
        verify(formVersionRepository).findByFormAndStatus(form, status);
        assertTrue(retrievedFormVersion.isPresent());
        assertEquals(expectedFormVersion, retrievedFormVersion.get());

        log.info("Leaving retrieveFormVersionByValidFormAndStatus()");
    }

    @Test
    @DisplayName("Retrieve FormVersion with Invalid Form and Status")
    void retrieveFormVersionWithInvalidFormAndStatus() {
        log.info("Entering retrieveFormVersionWithInvalidFormAndStatus()");

        // Given
        Form form = new Form();
        String status = "invalid";

        // Mock
        when(formVersionRepository.findByFormAndStatus(form, status)).thenReturn(Optional.empty());

        // When
        Optional<FormVersion> retrievedFormVersion = formVersionService.retrieveFormVersion(form, status);

        // Then
        verify(formVersionRepository).findByFormAndStatus(form, status);
        assertTrue(retrievedFormVersion.isEmpty());

        log.info("Leaving retrieveFormVersionWithInvalidFormAndStatus()");
    }

    @Test
    @DisplayName("Retrieve FormVersions for Valid Form")
    void retrieveFormVersionsForValidForm() {
        log.info("Entering retrieveFormVersionsForValidForm()");

        // Given
        Form form = new Form();
        List<FormVersion> expectedFormVersions = new ArrayList<>();
        expectedFormVersions.add(new FormVersion());
        expectedFormVersions.add(new FormVersion());

        // Mock the repository
        when(formVersionRepository.findByForm(form)).thenReturn(expectedFormVersions);

        // When
        List<FormVersion> retrievedFormVersions = formVersionService.retrieveFormVersions(form);

        // Then
        verify(formVersionRepository).findByForm(form);
        assertEquals(expectedFormVersions, retrievedFormVersions);

        log.info("Leaving retrieveFormVersionsForValidForm()");
    }

    @Test
    @DisplayName("Retrieve FormVersions for Form with No Versions")
    void retrieveFormVersionsForFormWithNoVersions() {
        log.info("Entering retrieveFormVersionsForFormWithNoVersions()");

        // Given
        Form form = new Form();

        // Mock the repository to return an empty list
        when(formVersionRepository.findByForm(form)).thenReturn(new ArrayList<>());

        // When
        List<FormVersion> retrievedFormVersions = formVersionService.retrieveFormVersions(form);

        // Then
        verify(formVersionRepository).findByForm(form);
        assertTrue(retrievedFormVersions.isEmpty());

        log.info("Leaving retrieveFormVersionsForFormWithNoVersions()");
    }
}