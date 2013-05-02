package com.github.cb372.persevere.impl;



import com.github.cb372.persevere.ExecutionResult;
import com.github.cb372.persevere.action.GiveUp;
import com.github.cb372.persevere.action.RetryableAction;
import com.github.cb372.persevere.delay.DelayStrategy;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: chris
 * Created: 5/2/13
 */
public class PersevereTask<T> implements Runnable {
    private final Config<T> config;
    private final int tryCount;

    public PersevereTask(Config<T> config, int tryCount) {
        this.config = config;
        this.tryCount = tryCount;
    }

    @Override
    public void run() {
        try {
            T result = config.action.execute(tryCount);
            config.future.markComplete(ExecutionResult.success(result, tryCount));
        } catch (GiveUp g) {
            config.future.markComplete(ExecutionResult.<T>failure(g, tryCount));
        } catch (Exception e) {
            if (canRetry()) {
                PersevereTask<T> nextTask = new PersevereTask<T>(config, tryCount + 1);
                config.executor.schedule(nextTask, config.delayStrategy.getNextDelayMs(tryCount), TimeUnit.MILLISECONDS);
            } else {
                config.future.markComplete(ExecutionResult.<T>failure(e, tryCount));
            }
        }
    }

    private boolean canRetry() {
        return (config.maxRetries < 0 || tryCount < config.maxRetries);
    }

    /**
     * The immutable configuration of a {@link com.github.cb372.persevere.impl.PersevereTask}.
     * This config is passed from one task to the next when performing retries.
     * @param <T> result type of the {@link com.github.cb372.persevere.action.RetryableAction}
     */
    protected static final class Config<T> {
        protected final RetryableAction<T> action;
        protected final PersevereFuture<T> future;
        protected final ScheduledExecutorService executor;
        protected final int maxRetries;
        protected final DelayStrategy delayStrategy;

        public Config(RetryableAction<T> action,
                      PersevereFuture<T> future,
                      ScheduledExecutorService executor,
                      int maxRetries,
                      DelayStrategy delayStrategy) {
            this.action = action;
            this.future = future;
            this.executor = executor;
            this.maxRetries = maxRetries;
            this.delayStrategy = delayStrategy;
        }
    }

}
