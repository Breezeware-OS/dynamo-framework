package net.breezeware.dynamo.usermanagement.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.entity.UserOrganizationMap;

@Repository
public interface UserOrganizationMapRepository extends GenericRepository<UserOrganizationMap> {
    Optional<UserOrganizationMap> findByUser(User user);
}
