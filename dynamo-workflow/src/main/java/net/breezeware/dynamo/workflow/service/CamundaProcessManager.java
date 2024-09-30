package net.breezeware.dynamo.workflow.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.workflow.dto.TaskDto;
import net.breezeware.dynamo.workflow.dto.TaskForm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Services to interface with the Camunda workflow engine.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaProcessManager {

    public static final String PROCESS_INSTANCE_USER_DEFINED_KEY = "processInstanceUserDefinedKey";

    public final RuntimeService runtimeService;

    public final HistoryService historyService;

    public final TaskService taskService;

    public final FormService formService;

    private final ObjectMapper objectMapper;

    @Transactional
    public void createNewProcessInstance(String processId, String processInstanceUserDefinedKey) {
        log.info("Entering createNewProcessInstance(). processId = {}, processInstanceUserDefinedKey = {}, ", processId,
                processInstanceUserDefinedKey);

        if (Objects.isNull(processId) || processId.isBlank()) {
            throw new DynamoException("Process ID must not be null.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(processInstanceUserDefinedKey) || processInstanceUserDefinedKey.isBlank()) {
            throw new DynamoException("Process Instance User Defined Key must not be null.", HttpStatus.BAD_REQUEST);
        }

        // create a new process instance with the updated set of variables
        try {
            ProcessInstance processInstance =
                    runtimeService.startProcessInstanceByKey(processId, processInstanceUserDefinedKey);
            runtimeService.setVariable(processInstance.getProcessInstanceId(), PROCESS_INSTANCE_USER_DEFINED_KEY,
                    processInstanceUserDefinedKey);
        } catch (ProcessEngineException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving createNewProcessInstance()");
    }

    /**
     * Retrieves the form associated for a given user task in the process instance.
     * @param  processId                     process definition id.
     * @param  processInstanceUserDefinedKey User defined key to uniquely identify
     *                                       the process instance created from a
     *                                       process definition
     * @param  taskDefinitionId              ID assigned to the task in the process
     *                                       definition
     * @return                               TaskForm entity containing information
     *                                       about the task form if one exists, else
     *                                       an empty object
     */
    @Transactional
    public Optional<TaskForm> retrieveTaskForm(String processId, String processInstanceUserDefinedKey,
            String taskDefinitionId) {
        log.info("""
                Entering retrieveTaskForm(). processId = {}, processInstanceUserDefinedKey= {}, taskDefinitionId = \
                {} \
                """, processId, processInstanceUserDefinedKey, taskDefinitionId);
        // retrieve a task that is currently active with specified parameters
        Optional<TaskDto> taskDtoOpt = retrieveActiveTask(processId, processInstanceUserDefinedKey, taskDefinitionId);
        if (taskDtoOpt.isPresent()) {
            TaskForm taskForm = retrieveTaskForm(taskDtoOpt.get());
            log.info("Leaving retrieveTaskForm() taskForm = {}", taskForm);
            return Optional.of(taskForm);
        } else {
            log.info("Leaving retrieveTaskForm(). TaskForm could not be retrieved since the task was not present.");
            return Optional.empty();
        }

    }

    private Optional<TaskDto> retrieveActiveTask(String processId, String processInstanceUserDefinedKey,
            String taskDefinitionId) {
        log.info("""
                Entering retrieveActiveTask(). ProcessId = {}, Process Instance user defined key =  {}, Task \
                Definition Id = {}\
                """, processId, processInstanceUserDefinedKey, taskDefinitionId);
        validProcessIdAndProcessInstanceUserDefinedKey(processId, processInstanceUserDefinedKey);

        if (Objects.isNull(taskDefinitionId) || taskDefinitionId.isBlank()) {
            throw new DynamoException("Task definition id must not be null or empty.", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<ProcessInstance> optionalProcessInstance =
                    retrieveProcessInstance(processId, processInstanceUserDefinedKey);
            if (optionalProcessInstance.isPresent()) {
                ProcessInstance processInstance = optionalProcessInstance.get();
                Task task = taskService.createTaskQuery().processInstanceBusinessKey(processInstance.getBusinessKey())
                        .taskDefinitionKey(taskDefinitionId).singleResult();
                // build TaskDto dto.
                TaskDto taskDto = TaskDto.builder().task(task)
                        .processInstanceUserDefinedKey(processInstanceUserDefinedKey).build();
                log.info("Leaving retrieveCurrentTask()");
                return Optional.ofNullable(taskDto);
            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new DynamoException("Error retrieving task from task service " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }

    }

    private Optional<ProcessInstance> retrieveProcessInstance(String processId, String processInstanceUserDefinedKey) {
        log.info("Entering retrieveProcessInstance()");
        validProcessIdAndProcessInstanceUserDefinedKey(processId, processInstanceUserDefinedKey);

        try {
            ProcessInstance processInstance =
                    runtimeService.createProcessInstanceQuery().processDefinitionKey(processId)
                            .processInstanceBusinessKey(processInstanceUserDefinedKey).active().singleResult();
            log.info("Leaving retrieveProcessInstance()");
            return Optional.ofNullable(processInstance);

        } catch (ProcessEngineException e) {
            log.error("Error retrieving process instance", e);
            throw new DynamoException("Error retrieving process instance: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    private TaskForm retrieveTaskForm(TaskDto taskDto) {
        log.info("Entering retrieveTaskForm()");
        JsonNode formData = retrieveFormJsonForTask(taskDto.getTask().getId());
        // build the entity
        TaskForm taskForm = TaskForm.builder().formSchemaAndDataJson(formData.toString())
                .processInstanceUserDefinedKey(taskDto.getProcessInstanceUserDefinedKey())
                .processName(taskDto.getTask().getProcessDefinitionId()).taskId(taskDto.getTask().getId())
                .taskDefinitionId(taskDto.getTask().getTaskDefinitionKey()).createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        log.info("Leaving retrieveTaskForm()");
        return taskForm;
    }

    /**
     * Retrieves the task form data for a specific task.
     * @param  taskId          The unique key of the task's identifier.
     * @return                 The JSON schema of the task's form data.
     * @throws DynamoException If the form is not found or there is an error
     *                         processing the form data.
     */
    private JsonNode retrieveFormJsonForTask(String taskId) {
        log.info("Entering retrieveFormJsonForTask() ");
        TaskFormData taskFormData = formService.getTaskFormData(taskId);
        if (Objects.isNull(taskFormData) || Objects.isNull(taskFormData.getCamundaFormRef())) {
            throw new DynamoException("Form not found for the task.", HttpStatus.BAD_REQUEST);
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(taskFormData.getCamundaFormRef().getKey());
            log.info("Leaving retrieveFormJsonForTask()");
            return jsonNode;
        } catch (JsonProcessingException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Persists the user task's form data and completes it.
     * @param taskForm TaskForm entity containing the form data submitted by the
     *                 user.
     */
    @Transactional
    public void completeUserTask(TaskForm taskForm) {
        log.info("Entering completeUserTask(). taskForm = {} ", taskForm);

        if (Objects.isNull(taskForm)) {
            throw new DynamoException("TaskForm must not be null.", HttpStatus.BAD_REQUEST);
        }

        // complete the task
        try {
            taskService.complete(taskForm.getTaskId());
            log.info("Leaving completeActiveTask()");
        } catch (ProcessEngineException e) {
            throw new DynamoException("Error completing active task: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving completeUserTask()");
    }

    @Transactional
    public void setVariables(String processInstanceKey, Map<String, Object> variablesMap) {
        log.info("Entering setVariables() processInstanceKey= {} variablesMap #size = {}  ", processInstanceKey,
                variablesMap.size());

        if (Objects.isNull(processInstanceKey) || processInstanceKey.isEmpty() || processInstanceKey.isBlank()) {
            throw new DynamoException("Process Instance  must not be null.", HttpStatus.BAD_REQUEST);
        }

        if (variablesMap.isEmpty()) {
            throw new DynamoException("Variables must be empty.", HttpStatus.BAD_REQUEST);
        }

        try {
            runtimeService.setVariables(processInstanceKey, variablesMap);
            log.info("Leaving setVariables()");

        } catch (ProcessEngineException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    public List<TaskDto> retrieveCurrentTasks(String processId, String processInstanceUserDefinedKey) {
        log.info("Entering retrieveCurrentTasks()");
        validProcessIdAndProcessInstanceUserDefinedKey(processId, processInstanceUserDefinedKey);
        Optional<ProcessInstance> optionalProcessInstance =
                retrieveProcessInstance(processId, processInstanceUserDefinedKey);
        try {
            List<TaskDto> taskDtoList = new ArrayList<>();
            if (optionalProcessInstance.isPresent()) {
                ProcessInstance processInstance = optionalProcessInstance.get();
                List<Task> tasks = taskService.createTaskQuery()
                        .processInstanceBusinessKey(processInstance.getBusinessKey()).list();
                for (Task task : tasks) {
                    TaskDto taskDto = TaskDto.builder().task(task)
                            .processInstanceUserDefinedKey(processInstanceUserDefinedKey).build();
                    taskDtoList.add(taskDto);
                }

            }

            log.info("Leaving retrieveCurrentTasks() {}", taskDtoList.size());
            return taskDtoList;
        } catch (BadUserRequestException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    private void validProcessIdAndProcessInstanceUserDefinedKey(String processId,
            String processInstanceUserDefinedKey) {
        if (Objects.isNull(processId) || processId.isBlank()) {
            throw new DynamoException("Process id must not be null or empty.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(processInstanceUserDefinedKey) || processInstanceUserDefinedKey.isBlank()) {
            throw new DynamoException("Process instance user id must not be null or empty.", HttpStatus.BAD_REQUEST);
        }

    }
}
