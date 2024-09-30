package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.usermanagement.dao.UserOrganizationMapRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.Organization;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserOrganizationMap;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("User Organization Map Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class UserOrganizationMapServiceTest {

    @Mock
    UserOrganizationMapRepository userOrganizationMapRepository;

    @InjectMocks
    UserOrganizationMapService userOrganizationMapService;

    @Test
    @DisplayName("Given valid userOrganizationMap to createUserOrganizationMap, then create a new userOrganizationMap")
    void givenValidUserOrganizationMap_when_createUserOrganizationMap_thenCreateUserOrganizationMap()
            throws DynamoException {
        log.info(
                "Entering givenValidUserOrganizationMap_when_createUserOrganizationMap_thenCreateUserOrganizationMap()");

        // Given
        UserOrganizationMap user = UserOrganizationMap.builder()
                .organization(Organization.builder().name("breezeware").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .build();

        // Mock
        UserOrganizationMap mockUserOrganizationMap = UserOrganizationMap.builder().id(1L)
                .organization(Organization.builder().name("breezeware").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userOrganizationMapRepository.save(eq(user))).thenReturn(mockUserOrganizationMap);

        // When
        UserOrganizationMap createdUserOrganizationMap = userOrganizationMapService.create(user);

        // Then
        verify(userOrganizationMapRepository).save(eq(user));
        assertThat(createdUserOrganizationMap.getId()).isNotNull();
        assertThat(createdUserOrganizationMap.getOrganization()).isNotNull();
        assertThat(createdUserOrganizationMap.getUser()).isEqualTo(mockUserOrganizationMap.getUser());
        assertThat(createdUserOrganizationMap.getOrganization()).isEqualTo(mockUserOrganizationMap.getOrganization());
        assertThat(createdUserOrganizationMap.getCreatedOn()).isNotNull();
        assertThat(createdUserOrganizationMap.getModifiedOn()).isNotNull();

        log.info(
                "Leaving givenValidUserOrganizationMap_when_createUserOrganizationMap_thenCreateUserOrganizationMap()");
    }

    @Test
    @DisplayName("Given a user, when retrieveUserOrganizationMap is called, then return the UserOrganizationMap if found")
    void givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnUserOrganizationMap() {
        log.info("Entering givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnUserOrganizationMap()");

        // Given
        User user = User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock
        UserOrganizationMap userOrganizationMap = UserOrganizationMap.builder().id(1L)
                .organization(Organization.builder().name("breezeware").build())
                .user(User.builder().id(1L).email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                        .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                        .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                        .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now())
                        .build())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userOrganizationMapRepository.findByUser(user)).thenReturn(Optional.of(userOrganizationMap));

        // When
        Optional<UserOrganizationMap> result = userOrganizationMapRepository.findByUser(user);

        // Then
        verify(userOrganizationMapRepository).findByUser(user);
        assertTrue(result.isPresent());
        assertSame(userOrganizationMap, result.get());

        log.info("Leaving givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnUserOrganizationMap()");
    }

    @Test
    @DisplayName("Given a user, when retrieveUserOrganizationMap is called, then return an empty Optional if not found")
    void givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnEmptyOptional() {
        log.info("Entering givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnEmptyOptional()");

        // Given
        User user = new User();

        // Mock
        when(userOrganizationMapRepository.findByUser(user)).thenReturn(Optional.empty());

        // When
        Optional<UserOrganizationMap> result = userOrganizationMapRepository.findByUser(user);

        // Then
        verify(userOrganizationMapRepository).findByUser(user);
        assertTrue(result.isEmpty());

        log.info("Leaving givenUser_whenRetrieveUserOrganizationMapCalled_thenReturnEmptyOptional()");
    }
}
