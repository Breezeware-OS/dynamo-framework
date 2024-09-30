package net.breezeware.dynamo.dynamodocssvc.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamodocssvc.dao.CollectionRepository;
import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CollectionService extends GenericService<Collection> {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        super(collectionRepository);
        this.collectionRepository = collectionRepository;
    }

    /**
     * Retrieves a collection by its unique identifier.
     * @param  uniqueId The unique identifier of the collection to retrieve.
     * @return          An Optional containing the collection if found, or empty if
     *                  not found.
     */
    public Optional<Collection> retrieveByUniqueId(UUID uniqueId) {
        log.info("Entering retrieveByUniqueId()");
        Optional<Collection> collection = collectionRepository.findByUniqueId(uniqueId);
        log.info("Leavings retrieveByUniqueId()");
        return collection;
    }

}
