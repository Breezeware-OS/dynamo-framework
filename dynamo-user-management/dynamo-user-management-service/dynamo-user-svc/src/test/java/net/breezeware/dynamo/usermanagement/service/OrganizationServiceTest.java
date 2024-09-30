package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.usermanagement.dao.OrganizationRepository;
import net.breezeware.dynamo.usermanagement.entity.Organization;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Organization Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    OrganizationRepository organizationRepository;

    @InjectMocks
    OrganizationService organizationService;

    @Test
    @DisplayName("Given valid organization to createOrganization, then create a new organization")
    void givenValidOrganization_when_createOrganization_thenCreateOrganization() throws DynamoException {
        log.info("Entering givenValidOrganization_when_createOrganization_thenCreateOrganization()");

        // Given
        Organization organization = Organization.builder().name("breezeware").build();

        // Mock
        Organization mockOrganization = Organization.builder().id(1L).name("breezeware").createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        when(organizationRepository.save(eq(organization))).thenReturn(mockOrganization);

        // When
        Organization createdorganization = organizationService.create(organization);

        // Then
        verify(organizationRepository).save(eq(organization));
        assertThat(createdorganization.getId()).isNotNull();
        assertThat(createdorganization.getCreatedOn()).isNotNull();
        assertThat(createdorganization.getModifiedOn()).isNotNull();
        assertThat(createdorganization.getName()).isEqualTo("breezeware");

        log.info("Leaving givenValidOrganization_when_createOrganization_thenCreateOrganization()");
    }

    @Test
    @DisplayName("Given valid organization name to retrieveOrganization, then return the organization")
    void givenValidOrganizationName_whenRetrieveOrganization_thenReturnOrganization() throws DynamoException {
        log.info("Entering givenValidOrganizationName_whenRetrieveOrganization_thenReturnOrganization()");

        // Given
        String organizationName = "org-name";

        // Mock
        Organization mockOrganization = Organization.builder().name(organizationName).build();
        when(organizationRepository.findByName(eq(organizationName))).thenReturn(Optional.of(mockOrganization));

        // When
        Optional<Organization> optOrganization = organizationService.retrieveOrganization(organizationName);

        // Then
        verify(organizationRepository).findByName(eq(organizationName));
        assertThat(optOrganization).isNotEmpty();

        log.info("Leaving givenValidOrganizationName_whenRetrieveOrganization_thenReturnOrganization()");
    }

}
