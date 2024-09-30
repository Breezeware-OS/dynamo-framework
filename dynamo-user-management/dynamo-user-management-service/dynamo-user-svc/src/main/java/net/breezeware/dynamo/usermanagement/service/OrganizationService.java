package net.breezeware.dynamo.usermanagement.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.OrganizationRepository;
import net.breezeware.dynamo.usermanagement.entity.Organization;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrganizationService extends GenericService<Organization> {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        super(organizationRepository);
        this.organizationRepository = organizationRepository;
    }

    public Optional<Organization> retrieveOrganization(String name) {
        return organizationRepository.findByName(name);
    }

}
