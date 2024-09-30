package net.breezeware.dynamo.dynamodocssvc.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamodocssvc.dao.DocumentRepository;
import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.dynamodocssvc.entity.Document;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentStatus;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocumentService extends GenericService<Document> {

    private final DocumentRepository documentRepository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param documentRepository the repository for accessing and managing entity
     *                           data.
     */
    public DocumentService(DocumentRepository documentRepository) {
        super(documentRepository);
        this.documentRepository = documentRepository;
    }

    /**
     * Retrieves a document by its unique identifier and status.
     * @param  uniqueId       The unique identifier of the document to retrieve.
     * @param  documentStatus The status of the document to retrieve.
     * @return                An Optional containing the document if found, or empty
     *                        if not found.
     */
    public Optional<Document> retrieveByUniqueIdAndStatus(UUID uniqueId, DocumentStatus documentStatus) {
        log.info("Entering retrieveByUniqueIdAndStatus");
        Optional<Document> document = documentRepository.findByUniqueIdAndStatus(uniqueId, documentStatus.getValue());
        log.info("Leaving retrieveByUniqueIdAndStatus()");
        return document;
    }

    /**
     * Retrieves a document by its unique identifier.
     * @param  uniqueId The unique identifier of the document to retrieve.
     * @return          An Optional containing the document if found, or empty if
     *                  not found.
     */
    public Optional<Document> retrieveByUniqueId(UUID uniqueId) {
        log.info("Entering retrieveByUniqueId()");
        Optional<Document> document = documentRepository.findByUniqueId(uniqueId);
        log.info("Leaving retrieveByUniqueId()");
        return document;
    }

    /**
     * Retrieves all documents belonging to a given collection.
     * @param  collection The collection for which to retrieve documents.
     * @return            A list of documents belonging to the specified collection.
     */
    public List<Document> retrieveByCollection(Collection collection) {
        log.info("Entering retrieveByCollection()");
        List<Document> documents = documentRepository.findByCollection(collection);
        log.info("Leaving retrieveByCollection()");
        return documents;
    }

}
