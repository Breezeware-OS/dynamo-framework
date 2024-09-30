package net.breezeware.dynamo.workflow.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity to hold information about the form that is associated with a user
 * task.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TaskForm {

    /**
     * ID generated and assigned by the Camunda workflow engine to uniquely identify
     * a single process instance created from the process definition.
     */
    private String processInstanceUserDefinedKey;
    /**
     * ID assigned by the Camunda workflow to uniquely identify a single task across
     * all process instances.
     */
    private String taskId;
    /**
     * Name of the process as defined in the 'id' attribute for bpmn:process element
     * in the BPMN model.
     */
    private String processName;
    private String taskDefinitionId;
    private String formSchemaAndDataJson;

    private Instant createdOn;
    private Instant modifiedOn;
}