package net.breezeware.dynamo.dynamoaisvc.dao;

import net.breezeware.dynamo.dynamoaisvc.entity.Model;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelRepository extends GenericRepository<Model> {
    Optional<Model> findByUniqueId(UUID uniqueId);
}