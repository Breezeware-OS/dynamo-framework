package net.breezeware.dynamo.batch.service;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch {@link Job} builder service.
 */
@Slf4j
@Service
public class JobBuilder {

    private static final RunIdIncrementer runIdIncrementer = new RunIdIncrementer();

    private final JobExecutionListener jobExecutionListener;

    public JobBuilder(JobExecutionListener jobExecutionListener) {
        this.jobExecutionListener = jobExecutionListener;
    }

    public Job build(String jobName, List<Step> steps, JobRepository jobRepository) throws IllegalStateException {
        log.debug("Entering build(), jobName = {}, # of steps in the job {}", jobName, steps.size());

        if (steps.isEmpty()) {
            log.info("No Steps to be configured in the job!");
            throw new IllegalStateException("Job not configured. Number of Steps should not be empty");
        }

        Step startStep = steps.get(0);
        org.springframework.batch.core.job.builder.JobBuilder jobBuilder =
                new org.springframework.batch.core.job.builder.JobBuilder(jobName, jobRepository);
        log.info("Configured job with the start step");
        if (steps.size() > 1) {
            log.info("Configuring job with multiple steps. # of steps = {}", steps.size() - 1);
            for (int i = 1; i < steps.size(); i++) {
                Step step = steps.get(i);
                jobBuilder.flow(step);
            }

        }

        Job job = jobBuilder.incrementer(runIdIncrementer).listener(jobExecutionListener).start(startStep).build();
        log.info("Configured job with listener. jobExecutionListener = {}", jobExecutionListener);
        log.debug("Leaving build(), job = {}", job);
        return job;
    }
}
