package net.breezeware.dynamo.usermanagement.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.UserOrganizationMapRepository;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserOrganizationMap;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserOrganizationMapService extends GenericService<UserOrganizationMap> {

    private final UserOrganizationMapRepository userOrganizationMapRepository;

    public UserOrganizationMapService(UserOrganizationMapRepository userOrganizationMapRepository) {
        super(userOrganizationMapRepository);
        this.userOrganizationMapRepository = userOrganizationMapRepository;
    }

    public Optional<UserOrganizationMap> retrieveUserOrganizationMap(User user) {
        log.info("Entering retrieveUserOrganizationMap()");
        Optional<UserOrganizationMap> userOrganizationMap = userOrganizationMapRepository.findByUser(user);
        log.info("Leaving retrieveUserOrganizationMap()");
        return userOrganizationMap;
    }
}
