package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao.FormInvitationRepository;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormInvitation;
import net.breezeware.dynamo.generics.crud.service.GenericService;

import lombok.extern.slf4j.Slf4j;

/**
 * This class represents a service for managing and retrieving FormResponse
 * entities. It extends the GenericService class, which provides common CRUD
 * operations.
 */
@Service
@Slf4j
public class FormInvitationSvcService extends GenericService<FormInvitation> {

    private final FormInvitationRepository formInvitationRepository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param formInvitationRepository the repository for accessing and managing
     *                                 entity data.
     */
    public FormInvitationSvcService(FormInvitationRepository formInvitationRepository) {
        super(formInvitationRepository);
        this.formInvitationRepository = formInvitationRepository;

    }

    public Optional<FormInvitation> retrieveFormInvitation(String email, long formId) {
        log.info("Entering retrieveFormInvitation() email= {}, formId= {}", email, formId);
        Optional<FormInvitation> optionalFormInvitation = formInvitationRepository.findByEmailAndFormId(email, formId);
        log.info("Leaving retrieveFormInvitation()");
        return optionalFormInvitation;
    }

}