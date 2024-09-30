package net.breezeware.dynamo.usermanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.UserRoleMapRepository;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserRoleMap;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserRoleMapService extends GenericService<UserRoleMap> {

    private final UserRoleMapRepository userRoleMapRepository;

    public UserRoleMapService(UserRoleMapRepository userRoleMapRepository) {
        super(userRoleMapRepository);
        this.userRoleMapRepository = userRoleMapRepository;
    }

    public List<UserRoleMap> retrieveUserRoleMap(User user) {
        log.info("Entering retrieveUserRoleMap()");
        List<UserRoleMap> userRoleMaps = userRoleMapRepository.findByUser(user);
        log.info("Leaving retrieveUserRoleMap()");
        return userRoleMaps;
    }
}
