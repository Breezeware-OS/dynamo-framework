package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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

import net.breezeware.dynamo.usermanagement.dao.RoleRepository;
import net.breezeware.dynamo.usermanagement.entity.Role;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Role Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    RoleService roleService;

    @Test
    @DisplayName("Given valid role to createRole, then create a new role")
    void givenValidRole_when_createRole_thenCreateRole() throws DynamoException {
        log.info("Entering givenValidRole_when_createRole_thenCreateRole()");

        // Given
        Role role = Role.builder().name("admin").build();

        // Mock
        Role mockRole = Role.builder().id(1L).name("admin").createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(roleRepository.save(eq(role))).thenReturn(mockRole);

        // When
        Role createdrole = roleService.create(role);

        // Then
        verify(roleRepository).save(eq(role));
        assertThat(createdrole.getId()).isNotNull();
        assertThat(createdrole.getCreatedOn()).isNotNull();
        assertThat(createdrole.getModifiedOn()).isNotNull();
        assertThat(createdrole.getName()).isEqualTo("admin");

        log.info("Leaving givenValidRole_when_createRole_thenCreateRole()");
    }

    @Test
    @DisplayName("Given valid roleName to retrieveRole, then return a retrieved role")
    public void givenValidRoleName_when_retrieveRole_thenReturnRetrievedRole() {
        log.info("Entering givenValidRoleName_when_retrieveRole_thenReturnRetrievedRole()");
        // Given
        String roleName = "admin";

        // Mock
        Role mockRole = Role.builder().id(1L).name("admin").createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock the RoleRepository behavior
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(mockRole));

        // When
        Optional<Role> retrievedRole = roleService.retrieveRole(roleName);

        // Then
        assertTrue(retrievedRole.isPresent());
        assertEquals(roleName, retrievedRole.get().getName());
        verify(roleRepository, times(1)).findByName(roleName);
        log.info("Leaving givenValidRoleName_when_retrieveRole_thenReturnRetrievedRole()");

    }

    @Test
    @DisplayName("Given Invalid roleName to retrieveRole, then return a retrieved role not found")
    public void givenInValidRoleName_when_retrieveRole_thenReturnRetrievedRoleNotFound() {
        log.info("Entering givenInValidRoleName_when_retrieveRole_thenReturnRetrievedRoleNotFound()");
        // Given
        String roleName = "invalid";

        // Mock
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When
        Optional<Role> retrievedRole = roleService.retrieveRole(roleName);

        // Then
        assertFalse(retrievedRole.isPresent());
        verify(roleRepository, times(1)).findByName(roleName);
        log.info("Leaving givenInValidRoleName_when_retrieveRole_thenReturnRetrievedRoleNotFound()");
    }
}
