package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao.FormResponseRepository;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormResponse;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a service for managing and retrieving FormResponse
 * entities. It extends the GenericService class, which provides common CRUD
 * operations.
 */
@Service
@Slf4j
public class FormResponseSvcService extends GenericService<FormResponse> {

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param formResponseRepository the repository for accessing and managing
     *                               entity data.
     */
    public FormResponseSvcService(FormResponseRepository formResponseRepository) {
        super(formResponseRepository);

    }

}