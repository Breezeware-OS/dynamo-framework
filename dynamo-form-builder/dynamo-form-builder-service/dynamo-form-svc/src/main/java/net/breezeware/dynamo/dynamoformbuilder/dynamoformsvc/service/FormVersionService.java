package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao.FormVersionRepository;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a service for managing and retrieving FormVersion
 * entities. It extends the GenericService class, which provides common CRUD
 * operations.
 */
@Service
@Slf4j
public class FormVersionService extends GenericService<FormVersion> {
    private final FormVersionRepository formVersionRepository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param formVersionRepository the repository for accessing and managing entity
     *                              data.
     */
    public FormVersionService(FormVersionRepository formVersionRepository) {
        super(formVersionRepository);
        this.formVersionRepository = formVersionRepository;
    }

    /**
     * Retrieves a FormVersion entity based on the provided Form and status.
     * @param  form   the Form entity to search for.
     * @param  status the status of the FormVersion to retrieve.
     * @return        an Optional containing the retrieved FormVersion, or an empty
     *                Optional if not found.
     */
    public Optional<FormVersion> retrieveFormVersion(Form form, String status) {
        log.info("Entering retrieveFormVersion(), form: {}, status: {}", form, status);
        Optional<FormVersion> retrievedFormVersion = formVersionRepository.findByFormAndStatus(form, status);
        log.info("Leaving retrieveFormVersion(), retrievedFormVersion: {}", retrievedFormVersion);
        return retrievedFormVersion;
    }

    /**
     * Retrieves a list of FormVersion entities associated with the provided Form.
     * @param  form the Form entity to retrieve FormVersions for.
     * @return      a List containing the retrieved FormVersions.
     */
    public List<FormVersion> retrieveFormVersions(Form form) {
        log.info("Entering retrieveFormVersions(), form: {}", form);
        List<FormVersion> retrievedFormVersions = formVersionRepository.findByForm(form);
        log.info("Leaving retrieveFormVersions(), retrievedFormVersions: {}", retrievedFormVersions);
        return retrievedFormVersions;
    }
}