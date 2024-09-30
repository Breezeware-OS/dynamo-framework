package net.breezeware.dynamo.batch.service;

import org.springframework.batch.item.ItemProcessor;

/**
 * Batch job step's {@link ItemProcessor}.<br>
 * Processes object from source to target type.
 * @param <T> type parameter of the source object.
 * @param <S> type parameter of the target object.
 */
public interface BaseItemProcessor<T, S> extends ItemProcessor<T, S> {
    @Override
    S process(T t) throws Exception;
}
