package net.breezeware.dynamo.usermanagement.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.UserRepository;
import net.breezeware.dynamo.usermanagement.entity.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService extends GenericService<User> {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    public Optional<User> retrieveUser(UUID userId) {
        log.info("Entering retrieveUser()");
        Optional<User> user = userRepository.findByUniqueId(userId);
        log.info("Leaving retrieveUser()");
        return user;
    }

    public Optional<User> retrieveUser(String email) {
        log.info("Entering retrieveUser()");
        Optional<User> user = userRepository.findByEmail(email);
        log.info("Leaving retrieveUser()");
        return user;
    }

    public List<User> retrieveUsersByGroup(List<String> groups) {
        log.info("Entering retrieveUsersByGroup()");
        List<User> users = userRepository.findByUserGroupMapGroupNameIn(groups);
        log.info("Leaving retrieveUsersByGroup()");
        return users;
    }

    public List<User> retrieveUsersByRole(List<String> roles) {
        log.info("Entering retrieveUsersByRole()");
        List<User> users = userRepository.findByUserRoleMapRoleNameIn(roles);
        log.info("Leaving retrieveUsersByRole()");
        return users;
    }

    public Optional<User> retrieveUserByIdmUserId(String idmUserId) {
        log.info("Entering retrieveUserByIdmUserId()");
        Optional<User> user = userRepository.findByIdmUserId(idmUserId);
        log.info("Leaving retrieveUserByIdmUserId()");
        return user;
    }

    public void createUserProfile(Object userData) {

    }
}
