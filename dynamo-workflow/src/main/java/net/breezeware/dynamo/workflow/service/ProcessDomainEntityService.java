package net.breezeware.dynamo.workflow.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.workflow.dao.ProcessDomainEntityRepository;
import net.breezeware.dynamo.workflow.entity.ProcessDomainEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessDomainEntityService {

    private final ProcessDomainEntityRepository processDomainEntityRepository;

    public Optional<ProcessDomainEntity> retrieveProcessDomainEntity(String processInstanceUserDefinitionKey,
            String entityName) {
        log.info("Entering retrieveProcessDomainEntity()");
        Optional<ProcessDomainEntity> optProcessDomainEntity = processDomainEntityRepository
                .findByProcessInstanceUserDefinitionKeyAndEntityName(processInstanceUserDefinitionKey, entityName);
        log.info("Leaving retrieveProcessDomainEntity()");
        return optProcessDomainEntity;
    }

    public List<ProcessDomainEntity> retrieveProcessDomainEntities(String processInstanceUserDefinitionKey) {
        log.info("Entering retrieveProcessDomainEntities()");
        List<ProcessDomainEntity> processDomainEntityList =
                processDomainEntityRepository.findByProcessInstanceUserDefinitionKey(processInstanceUserDefinitionKey);
        log.info("Leaving retrieveProcessDomainEntities()");
        return processDomainEntityList;
    }

    public ProcessDomainEntity saveProcessDomainEntity(ProcessDomainEntity processDomainEntity) {
        log.info("Entering saveProcessDomainEntity()");
        ProcessDomainEntity savedProcessDomainEntity = processDomainEntityRepository.save(processDomainEntity);
        log.info("Leaving saveProcessDomainEntity()");
        return savedProcessDomainEntity;
    }

}
