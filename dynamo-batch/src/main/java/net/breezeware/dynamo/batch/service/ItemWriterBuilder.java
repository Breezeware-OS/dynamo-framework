package net.breezeware.dynamo.batch.service;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch job step's {@link org.springframework.batch.item.ItemWriter} builder
 * service.<br>
 * Configured to build {@link JpaItemWriter}.<br>
 * <b>NOTE:</b> Requires a data source to be configured by default.
 */
@Slf4j
@Service
public class ItemWriterBuilder {

    private final EntityManagerFactory entityManagerFactory;

    public ItemWriterBuilder(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Builds a batch {@link JpaItemWriter} to write objects into the Datasource.
     * @param  targetClass target class type for the {@link JpaItemWriter}.
     * @param  <T>         type parameter of the {@link JpaItemWriter}.
     * @return             {@link JpaItemWriter}.
     */
    public <T> JpaItemWriter<T> build(Class<T> targetClass) {
        log.debug("Entering build(), targetClass = {}", targetClass);
        JpaItemWriter<T> jpaItemWriter =
                new JpaItemWriterBuilder<T>().entityManagerFactory(entityManagerFactory).build();
        log.debug("Leaving build(), jpaItemWriter = {}", jpaItemWriter);
        return jpaItemWriter;
    }
}
