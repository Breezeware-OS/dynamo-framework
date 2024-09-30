package net.breezeware.dynamo.usermanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.UserGroupMapRepository;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserGroupMap;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserGroupMapService extends GenericService<UserGroupMap> {

    private final UserGroupMapRepository userGroupMapRepository;

    public UserGroupMapService(UserGroupMapRepository userGroupMapRepository) {
        super(userGroupMapRepository);
        this.userGroupMapRepository = userGroupMapRepository;
    }

    public List<UserGroupMap> retrieveUserGroupMap(User user) {
        log.info("Entering retrieveUserGroupMap()");
        List<UserGroupMap> userGroupMaps = userGroupMapRepository.findByUser(user);
        log.info("Leaving retrieveUserGroupMap()");
        return userGroupMaps;
    }

}
