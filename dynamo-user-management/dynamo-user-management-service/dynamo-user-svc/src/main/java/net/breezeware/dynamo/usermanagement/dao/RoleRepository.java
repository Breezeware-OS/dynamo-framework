package net.breezeware.dynamo.usermanagement.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.Role;

@Repository
public interface RoleRepository extends GenericRepository<Role> {
    Optional<Role> findByName(String roleName);
}
