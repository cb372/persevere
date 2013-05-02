package com.github.cb372.persevere.impl;

import com.github.cb372.persevere.ExecutionResult;
import com.github.cb372.persevere.action.RetryableAction;
import com.github.cb372.persevere.delay.DelayStrategy;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The class that kick-starts the whole process.
 * Creates the first {@link PersevereTask} and submits it to the {@link ScheduledExecutorService}.
 * This starts a chain reaction where one task may create and submit the next one.
 *
 * Author: chris
 * Created: 4/23/13
 */
public final class PersevereRunner {
    private final ScheduledExecutorService executor;

    public PersevereRunner(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public <T> Future<ExecutionResult<T>> persevere(RetryableAction<T> action,
                                                    DelayStrategy delayStrategy,
                                                    int maxRetries) {
        PersevereFuture<T> future = new PersevereFuture<T>();
        PersevereTask.Config<T> config = new PersevereTask.Config<T>(action, future, executor, maxRetries, delayStrategy);
        Runnable firstTask = new PersevereTask<T>(config, 0);
        executor.submit(firstTask);
        return future;
    }

}
