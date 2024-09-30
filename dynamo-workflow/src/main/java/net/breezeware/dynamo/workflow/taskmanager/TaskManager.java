package net.breezeware.dynamo.workflow.taskmanager;

import net.breezeware.dynamo.workflow.dto.ProcessTaskData;
import net.breezeware.dynamo.workflow.dto.TaskForm;

public interface TaskManager {
    ProcessTaskData getInfoToActOnTask(String processId, String processInstanceUserDefinedKey, String taskDefinitionId);

    void completeTask(TaskForm taskForm);
}