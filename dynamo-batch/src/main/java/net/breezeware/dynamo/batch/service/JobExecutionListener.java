package net.breezeware.dynamo.batch.service;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch job execution listener.
 */
@Slf4j
@Component
public class JobExecutionListener implements org.springframework.batch.core.JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.debug("Entering afterJob(), jobExecution = {}", jobExecution);
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Batch job execution completed...");
        }

        log.debug("Leaving afterJob()");
    }
}
