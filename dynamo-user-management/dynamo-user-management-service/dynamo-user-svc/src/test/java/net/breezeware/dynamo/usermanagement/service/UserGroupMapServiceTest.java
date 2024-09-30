package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.usermanagement.dao.UserGroupMapRepository;
import net.breezeware.dynamo.usermanagement.entity.Group;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserGroupMap;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("User Group Map Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class UserGroupMapServiceTest {

    @Mock
    UserGroupMapRepository userGroupMapRepository;

    @InjectMocks
    UserGroupMapService userGroupMapService;

    @Test
    @DisplayName("Given valid userGroupMap to createUserGroupMap, then create a new userGroupMap")
    void givenValidUserGroupMap_when_createUserGroupMap_thenCreateUserGroupMap() throws DynamoException {
        log.info("Entering givenValidUserGroupMap_when_createUserGroupMap_thenCreateUserGroupMap()");

        // Given
        UserGroupMap user = UserGroupMap.builder().group(Group.builder().name("developer").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .build();

        // Mock
        UserGroupMap mockUserGroupMap = UserGroupMap.builder().id(1L).group(Group.builder().name("developer").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userGroupMapRepository.save(eq(user))).thenReturn(mockUserGroupMap);

        // When
        UserGroupMap createdUserGroupMap = userGroupMapService.create(user);

        // Then
        verify(userGroupMapRepository).save(eq(user));
        assertThat(createdUserGroupMap.getId()).isNotNull();
        assertThat(createdUserGroupMap.getGroup()).isNotNull();
        assertThat(createdUserGroupMap.getUser()).isEqualTo(mockUserGroupMap.getUser());
        assertThat(createdUserGroupMap.getGroup()).isEqualTo(mockUserGroupMap.getGroup());
        assertThat(createdUserGroupMap.getCreatedOn()).isNotNull();
        assertThat(createdUserGroupMap.getModifiedOn()).isNotNull();

        log.info("Leaving givenValidUserGroupMap_when_createUserGroupMap_thenCreateUserGroupMap()");
    }

    @Test
    @DisplayName("Given a user, when retrieveUserGroupMap is called, then user group maps should be retrieved")
    public void givenUser_whenRetrieveUserGroupMapCalled_thenRetrieveUserGroupMaps() {
        log.info("Entering givenUser_whenRetrieveUserGroupMapCalled_thenRetrieveUserGroupMaps()");

        // Mock
        User user = User.builder().id(1L).uniqueId(UUID.randomUUID()).build();

        List<UserGroupMap> expectedUserGroupMaps =
                List.of(UserGroupMap.builder().user(user).group(Group.builder().name("developer").build()).build());

        when(userGroupMapRepository.findByUser(user)).thenReturn(expectedUserGroupMaps);

        // When
        List<UserGroupMap> actualUserGroupMaps = userGroupMapService.retrieveUserGroupMap(user);

        // Verify
        verify(userGroupMapRepository).findByUser(user);

        // Then
        assertEquals(expectedUserGroupMaps, actualUserGroupMaps);

        log.info("Leaving givenUser_whenRetrieveUserGroupMapCalled_thenRetrieveUserGroupMaps()");
    }
}
