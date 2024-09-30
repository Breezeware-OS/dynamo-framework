package net.breezeware.dynamo.usermanagement.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.GroupRepository;
import net.breezeware.dynamo.usermanagement.entity.Group;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupService extends GenericService<Group> {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        super(groupRepository);
        this.groupRepository = groupRepository;
    }

    public Optional<Group> retrieveGroup(String groupName) {
        log.info("Entering retrieveGroup()");
        Optional<Group> group = groupRepository.findByName(groupName);
        log.info("Leaving retrieveGroup()");
        return group;
    }
}
