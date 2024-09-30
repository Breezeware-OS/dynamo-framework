package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao.FormRepository;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a service for managing and retrieving Form entities. It
 * extends the GenericService class, which provides common CRUD operations.
 */
@Service
@Slf4j
public class FormSvcService extends GenericService<Form> {

    private final FormRepository formRepository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param repository the repository for accessing and managing entity data.
     */
    public FormSvcService(FormRepository repository) {
        super(repository);
        this.formRepository = repository;
    }

    public Optional<Form> retrieveForm(String uniqueId) {
        log.info("Entering retrieveForm(), uniqueId: {}", uniqueId);
        Optional<Form> optForm = formRepository.findByUniqueId(uniqueId);
        log.info("Leaving retrieveForm(), form.#isPresent: {}", optForm.isPresent());
        return optForm;
    }
}
