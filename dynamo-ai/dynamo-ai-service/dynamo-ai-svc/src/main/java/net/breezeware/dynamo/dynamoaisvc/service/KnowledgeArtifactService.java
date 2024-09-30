package net.breezeware.dynamo.dynamoaisvc.service;

import lombok.extern.slf4j.Slf4j;
import net.breezeware.dynamo.dynamoaisvc.dao.KnowledgeArtifactRepository;
import net.breezeware.dynamo.dynamoaisvc.entity.KnowledgeArtifact;
import net.breezeware.dynamo.generics.crud.service.GenericService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class KnowledgeArtifactService extends GenericService<KnowledgeArtifact> {
    private final KnowledgeArtifactRepository knowledgeArtifactRepository;

    public KnowledgeArtifactService(KnowledgeArtifactRepository knowledgeArtifactRepository) {
        super(knowledgeArtifactRepository);
        this.knowledgeArtifactRepository = knowledgeArtifactRepository;
    }

    /**
     * Retrieves a knowledge artifact from the repository based on its unique ID.
     * @param  uniqueId the unique ID of the knowledge artifact to retrieve
     * @return          an Optional containing the knowledge artifact if found, or
     *                  empty if not found
     */
    public Optional<KnowledgeArtifact> retrieveKnowledgeArtifactByUniqueId(UUID uniqueId) {
        log.info("Entering retrieveKnowledgeArtifactByUniqueId()");
        Optional<KnowledgeArtifact> optionalKnowledgeArtifact = knowledgeArtifactRepository.findByUniqueId(uniqueId);
        log.info("Leaving retrieveKnowledgeArtifactByUniqueId()");
        return optionalKnowledgeArtifact;
    }

    /**
     * Deletes a knowledge artifact from the repository based on its unique ID and
     * AI model unique ID.
     * @param uniqueId      the unique ID of the knowledge artifact to delete
     * @param modelUniqueId the unique ID of the AI model associated with the
     *                      knowledge artifact
     */
    public void deleteKnowledgeArtifactByUniqueIdAndModelUniqueId(UUID uniqueId, UUID modelUniqueId) {
        log.info("Entering deleteKnowledgeArtifactByUniqueIdAndModelUniqueId()");
        knowledgeArtifactRepository.deleteByUniqueIdAndModelUniqueId(uniqueId, modelUniqueId);
        log.info("Leaving deleteKnowledgeArtifactByUniqueIdAndModelUniqueId()");
    }

    /**
     * Retrieves a KnowledgeArtifact by its unique ID and the model's unique ID.
     * @param  uniqueId      the unique ID of the knowledge artifact
     * @param  modelUniqueId the unique ID of the model
     * @return               an Optional containing the KnowledgeArtifact if found,
     *                       otherwise empty
     */
    public Optional<KnowledgeArtifact> retrieveKnowledgeArtifactByUniqueIdAndModelUniqueId(UUID uniqueId,
            UUID modelUniqueId) {
        log.info("Entering retrieveKnowledgeArtifactByUniqueIdAndModelUniqueId()");
        Optional<KnowledgeArtifact> optionalKnowledgeArtifact =
                knowledgeArtifactRepository.findByUniqueIdAndModelUniqueId(uniqueId, modelUniqueId);
        log.info("Leaving retrieveKnowledgeArtifactByUniqueIdAndModelUniqueId()");
        return optionalKnowledgeArtifact;
    }

    /**
     * Finds a list of KnowledgeArtifacts by the model's unique ID and their status.
     * @param  modelUniqueId the unique ID of the model
     * @param  status        the status of the knowledge artifacts
     * @return               a list of KnowledgeArtifacts matching the specified
     *                       model unique ID and status
     */
    public List<KnowledgeArtifact> findByModelUniqueIdAndStatus(UUID modelUniqueId, String status) {
        log.info("Entering findByModelUniqueIdAndStatus()");
        List<KnowledgeArtifact> knowledgeArtifacts =
                knowledgeArtifactRepository.findByModelUniqueIdAndStatus(modelUniqueId, status);
        log.info("Leaving findByModelUniqueIdAndStatus()");
        return knowledgeArtifacts;
    }

    /**
     * Finds and retrieves all knowledge artifacts associated with a specific AI
     * model.
     * @param  modelUniqueId the unique identifier of the AI model
     * @return               a list of knowledge artifacts associated with the
     *                       specified AI model
     */
    public List<KnowledgeArtifact> findByModelUniqueId(UUID modelUniqueId) {
        log.info("Entering findByModelUniqueId()");
        List<KnowledgeArtifact> knowledgeArtifacts = knowledgeArtifactRepository.findByModelUniqueId(modelUniqueId);
        log.info("Leaving findByModelUniqueId()");
        return knowledgeArtifacts;
    }

}
