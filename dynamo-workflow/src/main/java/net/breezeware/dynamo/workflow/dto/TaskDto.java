package net.breezeware.dynamo.workflow.dto;

import org.camunda.bpm.engine.task.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDto {
    private Task task;

    /**
     * Unique ID assigned to the process instance by Camunda workflow engine.
     */
    private String processInstanceUserDefinedKey;
}