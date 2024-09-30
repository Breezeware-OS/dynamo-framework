package net.breezeware.dynamo.batch.service;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Base Item execution/processing listener.
 * @param <T> type parameter of the source class.
 * @param <S> type parameter of the target class.
 */
@Slf4j
@Component
public class BaseItemExecutionListener<T, S> implements ItemProcessListener<T, S> {

    @Override
    public void beforeProcess(T t) {

    }

    @Override
    public void afterProcess(T t, S s) {
    }

    @Override
    public void onProcessError(T t, Exception e) {
    }
}
