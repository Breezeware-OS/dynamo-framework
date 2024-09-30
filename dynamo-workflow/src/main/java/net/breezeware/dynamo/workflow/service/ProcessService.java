package net.breezeware.dynamo.workflow.service;

import java.util.List;

import org.springframework.context.ApplicationContext;

import net.breezeware.dynamo.workflow.dto.ProcessTaskData;
import net.breezeware.dynamo.workflow.dto.TaskDto;
import net.breezeware.dynamo.workflow.dto.TaskForm;
import net.breezeware.dynamo.workflow.taskmanager.TaskManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An abstract base class for Camunda process services.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ProcessService {

    /**
     * The Camunda process manager for handling Camunda-related operations.
     */
    private final CamundaProcessManager camundaProcessManager;

    /**
     * The Spring application context for managing beans.
     */
    private final ApplicationContext applicationContext;

    /**
     * Starts a new instance of a BPMN process.
     * @return A unique identifier for the started process instance.
     */
    protected abstract String startProcess();

    /**
     * Retrieves a list of task definition IDs for the specific process.
     * @return A list of task definition IDs.
     */
    protected abstract List<String> getProcessTaskDefinitionIds();

    /**
     * Retrieves the unique identifier of the associated BPMN process.
     * @return The process identifier.
     */
    protected abstract String getProcessId();

    /**
     * Retrieves data related to a specific task in the process.
     * @param  processInstanceUserDefinedKey The user-defined key of the process
     *                                       instance.
     * @param  taskDefinitionId              The task definition ID of the task.
     * @return                               Process task data.
     */
    public ProcessTaskData retrieveProcessTaskData(String processInstanceUserDefinedKey, String taskDefinitionId) {
        log.info("Entering retrieveProcessTaskData()");
        String beanName = getProcessId() + "-" + taskDefinitionId;
        Object taskManagerBean = applicationContext.getBean(beanName);
        TaskManager taskManager = (TaskManager) taskManagerBean;
        ProcessTaskData processTaskData =
                taskManager.getInfoToActOnTask(getProcessId(), processInstanceUserDefinedKey, taskDefinitionId);
        log.info("Leaving retrieveProcessTaskData()");
        return processTaskData;
    }

    /**
     * Retrieves a list of currently active tasks in the process instance.
     * @param  processInstanceUserDefinedKey The user-defined key of the process
     *                                       instance.
     * @return                               A list of active tasks.
     */
    public List<TaskDto> retrieveCurrentActiveTasks(String processInstanceUserDefinedKey) {
        log.info("Entering retrieveCurrentActiveTasks()");
        List<TaskDto> taskDtoList =
                camundaProcessManager.retrieveCurrentTasks(getProcessId(), processInstanceUserDefinedKey);
        log.info("Leaving retrieveCurrentActiveTasks()");
        return taskDtoList;
    }

    /**
     * Completes a task in the process instance.
     * @param taskForm The task form data.
     */
    public void completeProcessTask(TaskForm taskForm) {
        log.info("Entering completeProcessTask()");
        String beanName = getProcessId() + "-" + taskForm.getTaskDefinitionId();
        Object taskManagerBean = applicationContext.getBean(beanName);
        TaskManager taskManager = (TaskManager) taskManagerBean;
        taskManager.completeTask(taskForm);
        log.info("Leaving completeProcessTask()");
    }

}
