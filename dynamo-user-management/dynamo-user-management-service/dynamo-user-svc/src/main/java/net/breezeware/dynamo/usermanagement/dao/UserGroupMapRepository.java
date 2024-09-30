package net.breezeware.dynamo.usermanagement.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserGroupMap;

@Repository
public interface UserGroupMapRepository extends GenericRepository<UserGroupMap> {
    List<UserGroupMap> findByUser(User user);
}
