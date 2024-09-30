package net.breezeware.dynamo.workflow.dto;

import java.util.Map;

import net.breezeware.dynamo.workflow.entity.ProcessDomainEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Information provided to the end user application to handle a process task.
 */

@Builder
@Data
public class ProcessTaskData {

    private Map<String, ProcessDomainEntity> contextData;

    private TaskForm taskForm;
}