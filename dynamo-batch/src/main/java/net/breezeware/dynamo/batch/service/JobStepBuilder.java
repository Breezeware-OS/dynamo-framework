package net.breezeware.dynamo.batch.service;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Batch job's {@link Step} builder service.
 */
@Slf4j
@Service
public class JobStepBuilder {

    private final StepExecutionListener stepExecutionListener;

    public JobStepBuilder(StepExecutionListener stepExecutionListener) {
        this.stepExecutionListener = stepExecutionListener;
    }

    /**
     * Builds a batch job {@link Step}.<br>
     * Configured with {@link StepExecutionListener} as the default listener for the
     * step.
     * @param  stepName            Name of the Step.
     * @param  itemReader          {@link ItemReader} for step.
     * @param  itemProcessor       {@link ItemProcessor} for step.
     * @param  itemProcessListener {@link BaseItemExecutionListener} for step.
     * @param  itemWriter          {@link ItemWriter} for step.
     * @param  chunkSize           Chunk size for {@link ItemWriter}.
     * @param  <T>                 Type parameter for {@link ItemProcessor} source.
     * @param  <S>                 Type parameter for {@link ItemProcessor} target.
     * @return                     {@link Step}.
     */
    public <T, S> Step build(String stepName, ItemReader<T> itemReader, ItemProcessor<T, S> itemProcessor,
            ItemProcessListener<T, S> itemProcessListener, ItemWriter<S> itemWriter, int chunkSize,
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.debug("""
                Entering build(), stepName = %s, itemProcessor = %s, itemProcessListener = %s, itemWriter = %s, \
                chunkSize = %d
                 """.formatted(stepName, itemProcessor, itemProcessListener, itemWriter, chunkSize));
        Step step = new StepBuilder(stepName, jobRepository).<T, S>chunk(chunkSize, transactionManager)
                .reader(itemReader).processor(itemProcessor).listener(itemProcessListener).writer(itemWriter)
                .listener(stepExecutionListener).build();
        log.debug("Leaving build(), step = {}", step);
        return step;
    }
}
