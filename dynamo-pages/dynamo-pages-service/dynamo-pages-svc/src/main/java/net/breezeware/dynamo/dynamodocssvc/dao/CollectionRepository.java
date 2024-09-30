package net.breezeware.dynamo.dynamodocssvc.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;

@Repository
public interface CollectionRepository extends GenericRepository<Collection> {

    Optional<Collection> findByUniqueId(UUID id);
}