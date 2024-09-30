package net.breezeware.dynamo.dynamodocssvc.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamodocssvc.dao.RevisionRepository;
import net.breezeware.dynamo.dynamodocssvc.entity.Document;
import net.breezeware.dynamo.dynamodocssvc.entity.Revision;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentStatus;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RevisionService extends GenericService<Revision> {

    private final RevisionRepository revisionRepository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param revisionRepository the repository for accessing and managing entity
     *                           data.
     */
    public RevisionService(RevisionRepository revisionRepository) {
        super(revisionRepository);
        this.revisionRepository = revisionRepository;
    }

    /**
     * Retrieves all revisions associated with a given document.
     * @param  updatedDocument The document for which to retrieve revisions.
     * @return                 A list of revisions associated with the specified
     *                         document.
     */
    public List<Revision> retrieveByDocument(Document updatedDocument) {
        log.info("Entering retrieveByDocument()");
        List<Revision> revision = revisionRepository.findByDocument(updatedDocument);
        log.info("Leaving retrieveByDocument()");
        return revision;
    }

    /**
     * Retrieves a revision of a document by its document and status.
     * @param  updatedDocument The document for which to retrieve the revision.
     * @param  documentStatus  The status of the document revision to retrieve.
     * @return                 An Optional containing the revision if found, or
     *                         empty if not found.
     */
    public Optional<Revision> retrieveByDocumentAndStatus(Document updatedDocument, DocumentStatus documentStatus) {
        log.info("Entering retrieveByDocumentAndStatus()");
        Optional<Revision> revision =
                revisionRepository.findByDocumentAndStatus(updatedDocument, documentStatus.getValue());
        log.info("Leaving retrieveByDocumentAndStatus()");
        return revision;
    }

}
