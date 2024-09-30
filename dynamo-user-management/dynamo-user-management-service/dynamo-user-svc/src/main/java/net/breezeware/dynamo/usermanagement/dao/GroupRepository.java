package net.breezeware.dynamo.usermanagement.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.Group;

@Repository
public interface GroupRepository extends GenericRepository<Group> {
    Optional<Group> findByName(String groupName);
}
