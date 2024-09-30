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

import net.breezeware.dynamo.usermanagement.dao.UserRoleMapRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.Role;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserRoleMap;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("User Role Map Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class UserRoleMapServiceTest {

    @Mock
    UserRoleMapRepository userRoleMapRepository;

    @InjectMocks
    UserRoleMapService userRoleMapService;

    @Test
    @DisplayName("Given valid userRoleMap to createUserRoleMap, then create a new userRoleMap")
    void givenValidUserRoleMap_when_createUserRoleMap_thenCreateUserRoleMap() throws DynamoException {
        log.info("Entering givenValidUserRoleMap_when_createUserRoleMap_thenCreateUserRoleMap()");

        // Given
        UserRoleMap user = UserRoleMap.builder().role(Role.builder().name("admin").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .build();

        // Mock
        UserRoleMap mockUserRoleMap = UserRoleMap.builder().id(1L).role(Role.builder().name("admin").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userRoleMapRepository.save(eq(user))).thenReturn(mockUserRoleMap);

        // When
        UserRoleMap createdUserRoleMap = userRoleMapService.create(user);

        // Then
        verify(userRoleMapRepository).save(eq(user));
        assertThat(createdUserRoleMap.getId()).isNotNull();
        assertThat(createdUserRoleMap.getRole()).isNotNull();
        assertThat(createdUserRoleMap.getUser()).isEqualTo(mockUserRoleMap.getUser());
        assertThat(createdUserRoleMap.getRole()).isEqualTo(mockUserRoleMap.getRole());
        assertThat(createdUserRoleMap.getCreatedOn()).isNotNull();
        assertThat(createdUserRoleMap.getModifiedOn()).isNotNull();

        log.info("Leaving givenValidUserRoleMap_when_createUserRoleMap_thenCreateUserRoleMap()");
    }

    @Test
    @DisplayName("Given a user, when retrieveUserRoleMap is called, then user role maps should be retrieved")
    public void givenUser_whenRetrieveUserRoleMapCalled_thenRetrieveUserRoleMaps() {
        log.info("Entering givenUser_whenRetrieveUserRoleMapCalled_thenRetrieveUserRoleMaps()");

        // Mock
        User user = User.builder().id(1L).uniqueId(UUID.randomUUID()).build();

        List<UserRoleMap> expectedUserRoleMaps =
                List.of(UserRoleMap.builder().user(user).role(Role.builder().name("user").build()).build());

        when(userRoleMapRepository.findByUser(user)).thenReturn(expectedUserRoleMaps);

        // When
        List<UserRoleMap> actualUserRoleMaps = userRoleMapService.retrieveUserRoleMap(user);

        // Verify
        verify(userRoleMapRepository).findByUser(user);

        // Then
        assertEquals(expectedUserRoleMaps, actualUserRoleMaps);

        log.info("Leaving givenUser_whenRetrieveUserRoleMapCalled_thenRetrieveUserRoleMaps()");
    }
}
