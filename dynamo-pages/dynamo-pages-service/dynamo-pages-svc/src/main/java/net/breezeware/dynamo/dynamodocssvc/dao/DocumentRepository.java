package net.breezeware.dynamo.dynamodocssvc.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.dynamodocssvc.entity.Document;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;

@Repository
public interface DocumentRepository extends GenericRepository<Document> {
    Optional<Document> findByTitle(String name);

    Optional<Document> findByUniqueIdAndStatus(UUID uniqueId, String status);

    Optional<Document> findByUniqueId(UUID uniqueId);

    List<Document> findByCollection(Collection collection);
}
