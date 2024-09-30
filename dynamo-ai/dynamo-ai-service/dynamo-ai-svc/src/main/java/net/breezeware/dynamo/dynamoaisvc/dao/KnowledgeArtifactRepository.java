package net.breezeware.dynamo.dynamoaisvc.dao;

import net.breezeware.dynamo.dynamoaisvc.entity.KnowledgeArtifact;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeArtifactRepository extends GenericRepository<KnowledgeArtifact> {
    Optional<KnowledgeArtifact> findByUniqueId(UUID uniqueId);

    void deleteByUniqueIdAndModelUniqueId(UUID uniqueId, UUID modelUniqueId);

    Optional<KnowledgeArtifact> findByUniqueIdAndModelUniqueId(UUID uniqueId, UUID modelUniqueId);

    List<KnowledgeArtifact> findByModelUniqueIdAndStatus(UUID modelUniqueId, String status);

    List<KnowledgeArtifact> findByModelUniqueId(UUID modelUniqueId);

}