package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.usermanagement.dao.UserRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.usermanagement.entity.QUser;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.enumeration.UserStatus;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("User Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("Given valid user to createUser, then create a new user")
    void givenValidUser_when_createUser_thenCreateUser() throws DynamoException {
        log.info("Entering givenValidUser_when_createUser_thenCreateUser()");

        // Given
        User user = User.builder().email("john@examble.com").idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).build();

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        when(userRepository.save(eq(user))).thenReturn(mockUser);

        // When
        User createduser = userService.create(user);

        // Then
        verify(userRepository).save(eq(user));
        assertThat(createduser.getId()).isNotNull();
        assertThat(createduser.getUniqueId()).isNotNull();
        assertThat(createduser.getUniqueId()).isEqualTo(mockUser.getUniqueId());
        assertThat(createduser.getCreatedOn()).isNotNull();
        assertThat(createduser.getModifiedOn()).isNotNull();
        assertThat(createduser.getStatus()).isEqualTo("invited");

        log.info("Leaving givenValidUser_when_createUser_thenCreateUser()");
    }

    @Test
    @DisplayName("Given valid userId to retrieveUser, then return a retrieved user")
    public void givenValidUserId_when_retrieveUser_thenReturnRetrievedUser() {
        log.info("Entering givenValidUserId_when_retrieveUser_thenReturnRetrievedUser()");
        // Given
        UUID userId = UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5");

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock the UserRepository behavior
        when(userRepository.findByUniqueId(userId)).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> retrievedUser = userService.retrieveUser(userId);

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(userId, retrievedUser.get().getUniqueId());
        verify(userRepository, times(1)).findByUniqueId(userId);
        log.info("Leaving givenValidUserId_when_retrieveUser_thenReturnRetrievedUser()");

    }

    @Test
    @DisplayName("Given Invalid userId to retrieveUser, then return a retrieved user not found")
    public void givenInValidUserId_when_retrieveUser_thenReturnRetrievedUserNotFound() {
        log.info("Entering givenInValidUserId_when_retrieveUser_thenReturnRetrievedUserNotFound()");
        // Given
        UUID userId = UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5");

        // Mock
        when(userRepository.findByUniqueId(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> retrievedUser = userService.retrieveUser(userId);

        // Then
        assertFalse(retrievedUser.isPresent());
        verify(userRepository, times(1)).findByUniqueId(userId);
        log.info("Leaving givenInValidUserId_when_retrieveUser_thenReturnRetrievedUserNotFound()");
    }

    @Test
    @DisplayName("Given valid userEmail to retrieveUser, then return a retrieved user")
    public void givenValidUserEmail_when_retrieveUser_thenReturnRetrievedUser() {
        log.info("Entering givenValidUserEmail_when_retrieveUser_thenReturnRetrievedUser()");
        // Given
        String email = "john@examble.com";

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock the UserRepository behavior
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> retrievedUser = userService.retrieveUser(email);

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(email, retrievedUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        log.info("Leaving givenValidUserEmail_when_retrieveUser_thenReturnRetrievedUser()");

    }

    @Test
    @DisplayName("Given Invalid user email to retrieveUser, then return a retrieved user not found")
    public void givenInValidUserEmail_when_retrieveUser_thenReturnRetrievedUserNotFound() {
        log.info("Entering givenInValidUserEmail_when_retrieveUser_thenReturnRetrievedUserNotFound()");
        // Given
        String email = "john@examble.com";

        // Mock
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> retrievedUser = userService.retrieveUser(email);

        // Then
        assertFalse(retrievedUser.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
        log.info("Leaving givenInValidUserEmail_when_retrieveUser_thenReturnRetrievedUserNotFound()");
    }

    @Test
    @DisplayName("Given userName as search/filter predicate and pageable to retrievePageEntitiesWithPredicate, then return filtered/searched page of Users")
    void givenUserNameSearchPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers() {
        log.info(
                "Entering givenUserNameSearchPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers()");

        // Given
        String userName = "john";

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529").firstName("john")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        List<User> userList = List.of(mockUser, User.builder().id(2L).email("alex@examble.com").firstName("alex")
                .idmUserId("9e3f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a2d82416-3e79-4232-bb6b-0785319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build());

        // pagination and sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 2, sort);
        // filtering or searching
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QUser.user.firstName.containsIgnoreCase(userName))
                .or(QUser.user.lastName.containsIgnoreCase(userName));

        // Mock
        var filteredUserList = userList.stream().filter(user -> user.getFirstName().equals(userName)).toList();
        PageImpl<User> mockPagedUser = new PageImpl<>(filteredUserList, pageable, userList.size());

        when(userRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(mockPagedUser);

        // When
        Page<User> pagedUser = userService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);
        log.info("pagedUser: {}", pagedUser.stream().toList());

        // Then
        verify(userRepository).findAll(booleanBuilder, pageable);
        assertThat(pagedUser).isNotEmpty();
        assertThat(pagedUser.getContent()).containsExactly(mockUser);

        log.info(
                "Leaving givenUserNameSearchPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers()");
    }

    @Test
    @DisplayName("Given status as filter predicate and pageable to retrievePageEntitiesWithPredicate, then return filtered page of users")
    void givenUserStatusFilterPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers() {
        log.info(
                "Entering givenUserStatusFilterPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers()");

        // Given
        String userStatus = "active";

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529").firstName("john")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5")).status(UserStatus.ACTIVE.getStatus())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build();
        List<User> userList = List.of(mockUser, User.builder().id(2L).email("alex@examble.com").firstName("alex")
                .idmUserId("9e3f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a2d82416-3e79-4232-bb6b-0785319b07b5")).status(UserStatus.ACTIVE.getStatus())
                .createdOn(Instant.now()).modifiedOn(Instant.now()).build());

        // pagination and sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "email");
        Pageable pageable = PageRequest.of(0, 2, sort);
        // filtering or searching
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QUser.user.status.equalsIgnoreCase(userStatus));

        // Mock
        var filteredUserList = userList.stream().filter(user -> user.getStatus().equalsIgnoreCase(userStatus)).toList();
        PageImpl<User> mockPagedUser = new PageImpl<>(filteredUserList, pageable, userList.size());

        when(userRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(mockPagedUser);

        // When
        Page<User> pagedUser = userService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);
        log.info("pagedUser: {}", pagedUser.stream().toList());

        // Then
        verify(userRepository).findAll(booleanBuilder, pageable);
        assertThat(pagedUser).isNotEmpty();
        assertThat(pagedUser.getContent()).containsAll(userList);

        log.info(
                "Leaving givenUserStatusFilterPredicateAndPageable_whenRetrievePageEntitiesWithPredicate_thenReturnFilteredPagedUsers()");
    }

    @Test
    @DisplayName("GivenValidRoles_WhenRetrieveUsers_ThenReturnUsers")
    public void givenValidGroups_whenRetrieveUsers_thenReturnUsers() {
        log.info("Entering givenValidGroups_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> validGroups = Arrays.asList("group1", "group2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());

        // Mock
        when(userRepository.findByUserGroupMapGroupNameIn(validGroups)).thenReturn(expectedUsers);

        // When
        List<User> resultUsers = userService.retrieveUsersByGroup(validGroups);

        // Then
        assertEquals(expectedUsers, resultUsers);
        verify(userRepository, times(1)).findByUserGroupMapGroupNameIn(validGroups);

        log.info("Leaving givenValidGroups_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("GivenNullGroups_WhenRetrieveUsers_ThenReturnEmptyUsers")
    public void givenNullGroups_whenRetrieveUsers_thenReturnEmptyUsers() {
        log.info("Entering givenNullGroups_whenRetrieveUsers_thenReturnEmptyUsers()");

        // Given
        List<String> nullGroups = null;

        // When
        List<User> resultUsers = userService.retrieveUsersByGroup(nullGroups);

        // Then
        assertEquals(0, resultUsers.size());
        verify(userRepository, times(1)).findByUserGroupMapGroupNameIn(nullGroups);

        log.info("Leaving givenNullGroups_whenRetrieveUsers_thenReturnEmptyUsers()");
    }

    @Test
    @DisplayName("GivenValidRoles_WhenRetrieveUsers_ThenReturnUsers")
    public void givenValidRoles_whenRetrieveUsers_thenReturnUsers() {
        log.info("Entering givenValidRoles_whenRetrieveUsers_thenReturnUsers()");

        // Given
        List<String> validRoles = Arrays.asList("role1", "role2");
        List<User> expectedUsers = Arrays.asList(new User(), new User());

        // Mock
        when(userRepository.findByUserRoleMapRoleNameIn(validRoles)).thenReturn(expectedUsers);

        // When
        List<User> resultUsers = userService.retrieveUsersByRole(validRoles);

        // Then
        assertEquals(expectedUsers, resultUsers);
        verify(userRepository, times(1)).findByUserRoleMapRoleNameIn(validRoles);

        log.info("Leaving givenValidRoles_whenRetrieveUsers_thenReturnUsers()");
    }

    @Test
    @DisplayName("GivenNullRoles_WhenRetrieveUsers_ThenReturnEmptyUsers")
    public void givenNullRoles_whenRetrieveUsers_thenReturnEmptyUsers() {
        log.info("Entering givenNullRoles_whenRetrieveUsers_thenReturnEmptyUsers()");

        // Given
        List<String> nullRoles = null;

        // When
        List<User> resultUsers = userService.retrieveUsersByRole(nullRoles);

        // Then
        assertEquals(0, resultUsers.size());
        verify(userRepository, times(1)).findByUserRoleMapRoleNameIn(nullRoles);

        log.info("Leaving givenNullRoles_whenRetrieveUsers_thenReturnEmptyUsers()");
    }

    @Test
    @DisplayName("Given valid Idm userId to retrieveUser, then return a retrieved user")
    public void givenValidIdmUserId_when_retrieveUser_thenReturnRetrievedUser() {
        log.info("Entering givenValidIdmUserId_when_retrieveUser_thenReturnRetrievedUser()");
        // Given
        String idmUSerId = "0e4f5855-e738-4b24-aa33-2098a0ace529";

        // Mock
        User mockUser = User.builder().id(1L).email("john@examble.com")
                .idmUserId("0e4f5855-e738-4b24-aa33-2098a0ace529")
                .idmInfo(IdmInfo.builder().idmUniqueId("unique_id").name("cognito").build())
                .uniqueId(UUID.fromString("a7d82416-3e79-4232-bb6b-0094319b07b5"))
                .status(UserStatus.INVITED.getStatus()).createdOn(Instant.now()).modifiedOn(Instant.now()).build();

        // Mock the UserRepository behavior
        when(userRepository.findByIdmUserId(idmUSerId)).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> retrievedUser = userService.retrieveUserByIdmUserId(idmUSerId);

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(idmUSerId, retrievedUser.get().getIdmUserId());
        verify(userRepository, times(1)).findByIdmUserId(idmUSerId);
        log.info("Leaving givenValidIdmUserId_when_retrieveUser_thenReturnRetrievedUser()");

    }

    @Test
    @DisplayName("Given Invalid idm user id to retrieveUser, then return a retrieved user not found")
    public void givenInValidIdmUserId_when_retrieveUser_thenReturnRetrievedUserNotFound() {
        log.info("Entering givenInValidIdmUserId_when_retrieveUser_thenReturnRetrievedUserNotFound()");
        // Given
        String idmUSerId = "09";

        // Mock
        when(userRepository.findByIdmUserId(idmUSerId)).thenReturn(Optional.empty());

        // When
        Optional<User> retrievedUser = userService.retrieveUserByIdmUserId(idmUSerId);

        // Then
        assertFalse(retrievedUser.isPresent());
        verify(userRepository, times(1)).findByIdmUserId(idmUSerId);
        log.info("Leaving givenInValidIdmUserId_when_retrieveUser_thenReturnRetrievedUserNotFound()");
    }
}
