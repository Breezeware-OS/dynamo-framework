package net.breezeware.dynamo.usermanagement.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.Organization;

@Repository
public interface OrganizationRepository extends GenericRepository<Organization> {
    Optional<Organization> findByName(String name);
}
