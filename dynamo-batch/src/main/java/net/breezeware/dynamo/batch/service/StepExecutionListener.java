package net.breezeware.dynamo.batch.service;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch job's step execution listener.
 */
@Slf4j
@Component
public class StepExecutionListener implements org.springframework.batch.core.StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug("Entering beforeStep(), stepExecution = {}", stepExecution);
        String stepName = stepExecution.getStepName();
        LocalDateTime startTime = stepExecution.getStartTime();
        log.info("Started step '{}' execution, startTime = {}", stepName, startTime);
        log.debug("Leaving beforeStep()");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("Entering afterStep(), stepExecution = {}", stepExecution);
        String stepName = stepExecution.getStepName();
        Instant endInstant = Instant.now();
        log.info("Completed step '{}' execution, endInstant = {}", stepName, endInstant);
        log.debug("Leaving afterStep()");
        return stepExecution.getJobExecution().getExitStatus();
    }
}
