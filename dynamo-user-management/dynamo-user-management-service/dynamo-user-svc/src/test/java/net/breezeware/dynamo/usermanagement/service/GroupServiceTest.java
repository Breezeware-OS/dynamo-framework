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

import net.breezeware.dynamo.usermanagement.dao.GroupRepository;
import net.breezeware.dynamo.usermanagement.entity.Group;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Group Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    GroupRepository groupRepository;

    @InjectMocks
    GroupService groupService;

    @Test
    @DisplayName("Given valid group to createGroup, then create a new group")
    void givenValidGroup_when_createGroup_thenCreateGroup() throws DynamoException {
        log.info("Entering givenValidGroup_when_createGroup_thenCreateGroup()");

        // Given
        Group group = Group.builder().name("developer").build();

        // Mock
        Group mockGroup =
                Group.builder().id(1L).name("developer").createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(groupRepository.save(eq(group))).thenReturn(mockGroup);

        // When
        Group createdgroup = groupService.create(group);

        // Then
        verify(groupRepository).save(eq(group));
        assertThat(createdgroup.getId()).isNotNull();
        assertThat(createdgroup.getCreatedOn()).isNotNull();
        assertThat(createdgroup.getModifiedOn()).isNotNull();
        assertThat(createdgroup.getName()).isEqualTo("developer");

        log.info("Leaving givenValidGroup_when_createGroup_thenCreateGroup()");
    }

    @Test
    @DisplayName("Given valid groupName to retrieveGroup, then return a retrieved group")
    public void givenValidGroupName_when_retrieveGroup_thenReturnRetrievedGroup() {
        log.info("Entering givenValidGroupName_when_retrieveGroup_thenReturnRetrievedGroup()");
        // Given
        String groupName = "developer";

        // Mock
        Group mockGroup =
                Group.builder().id(1L).name("developer").createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock the GroupRepository behavior
        when(groupRepository.findByName(groupName)).thenReturn(Optional.of(mockGroup));

        // When
        Optional<Group> retrievedGroup = groupService.retrieveGroup(groupName);

        // Then
        assertTrue(retrievedGroup.isPresent());
        assertEquals(groupName, retrievedGroup.get().getName());
        verify(groupRepository, times(1)).findByName(groupName);
        log.info("Leaving givenValidGroupName_when_retrieveGroup_thenReturnRetrievedGroup()");

    }

    @Test
    @DisplayName("Given Invalid groupName to retrieveGroup, then return a retrieved group not found")
    public void givenInValidGroupName_when_retrieveGroup_thenReturnRetrievedGroupNotFound() {
        log.info("Entering givenInValidGroupName_when_retrieveGroup_thenReturnRetrievedGroupNotFound()");
        // Given
        String groupName = "invalid";

        // Mock
        when(groupRepository.findByName(groupName)).thenReturn(Optional.empty());

        // When
        Optional<Group> retrievedGroup = groupService.retrieveGroup(groupName);

        // Then
        assertFalse(retrievedGroup.isPresent());
        verify(groupRepository, times(1)).findByName(groupName);
        log.info("Leaving givenInValidGroupName_when_retrieveGroup_thenReturnRetrievedGroupNotFound()");
    }
}
