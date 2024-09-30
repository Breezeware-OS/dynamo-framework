package net.breezeware.dynamo.workflow.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.workflow.entity.ProcessDomainEntity;

@Repository
public interface ProcessDomainEntityRepository extends JpaRepository<ProcessDomainEntity, Long> {

    Optional<ProcessDomainEntity> findByProcessInstanceUserDefinitionKeyAndEntityName(
            String processInstanceUserDefinitionKey, String entityName);

    List<ProcessDomainEntity> findByProcessInstanceUserDefinitionKey(String processInstanceUserDefinitionKey);
}