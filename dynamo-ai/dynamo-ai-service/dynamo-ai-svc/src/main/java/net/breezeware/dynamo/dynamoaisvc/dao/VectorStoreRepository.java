package net.breezeware.dynamo.dynamoaisvc.dao;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.dynamoaisvc.entity.VectorStore;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VectorStoreRepository extends GenericRepository<VectorStore> {
    void deleteByModelUniqueIdAndKnowledgeArtifactUniqueId(UUID modelUniqueId, UUID knowledgeArtifactUniqueId);

    @Query(value = "SELECT *, embedding <=> CAST(:embedding AS vector) AS distance " + "FROM dynamo_ai.vector_store "
            + "WHERE embedding <=> CAST(:embedding AS vector) < :threshold AND model_id = :modelId "
            + "ORDER BY distance " + "LIMIT :limit", nativeQuery = true)
    List<VectorStore> findRelevantDocuments(@Param("embedding") float[] embedding, @Param("modelId") UUID modelId,
            @Param("threshold") double threshold, @Param("limit") int limit);
}