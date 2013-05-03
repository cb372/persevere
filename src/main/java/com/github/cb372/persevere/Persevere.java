package com.github.cb372.persevere;

import com.github.cb372.persevere.action.RetryableAction;
import com.github.cb372.persevere.delay.DelayStrategy;
import com.github.cb372.persevere.impl.PersevereRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Author: chris
 * Created: 5/2/13
 */
public final class Persevere {
    private static volatile ScheduledExecutorService executor = null;
    private static volatile boolean createdOurOwnExecutor = false;

    private Persevere() {
        // static methods only
    }

    public static void init(int threadPoolSize) {
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be >= 1");
        }
        Persevere.executor = new ScheduledThreadPoolExecutor(threadPoolSize);
        Persevere.createdOurOwnExecutor = true;
    }

    public static void init(ScheduledExecutorService executor) {
        if (Persevere.executor != null) {
            throw new IllegalStateException("Persevere is already initialized");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Please provide a ScheduledExecutorService");
        }
        Persevere.executor = executor;
        Persevere.createdOurOwnExecutor = false;
    }

    /**
     * Perform the given action, retrying if it fails.
     *
     * @param action The action to perform
     * @param delayStrategy The strategy for inserting delays between retries
     * @param maxRetries
     *   The maximum number of times to retry.
     *   If this is 0, the action will be tried once and not retried.
     *   If it is negative, the action will be retried indefinitely until it succeeds.
     * @param <T> result type
     * @return a future of the result of performing the action
     */
    public static <T> Future<ExecutionResult<T>> persevere(RetryableAction<T> action,
                                                           DelayStrategy delayStrategy,
                                                           int maxRetries) {
        if (Persevere.executor == null) {
            throw new IllegalStateException("Persevere has not been initialized. Please call init() first.");
        }
        return new PersevereRunner(executor).persevere(action, delayStrategy, maxRetries);
    }

    public static void shutdown() {
        if (Persevere.executor != null && Persevere.createdOurOwnExecutor) {
            Persevere.executor.shutdown();
        }
    }

}
