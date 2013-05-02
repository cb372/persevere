package com.github.cb372.persevere.impl;

import com.github.cb372.persevere.ExecutionResult;

import java.util.concurrent.*;

/**
 * Author: chris
 * Created: 5/2/13
 */
public class PersevereFuture<T> implements Future<ExecutionResult<T>> {
    private final CountDownLatch complete = new CountDownLatch(1);
    private volatile ExecutionResult<T> result;

    /**
     * Mark the future as complete (either succeeded or failed).
     * After this method has been called, {@link #isDone()} will return true.
     *
     * @param result the result of performing the action
     */
    protected void markComplete(ExecutionResult<T> result) {
        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }
        if (complete.getCount() == 0) {
            throw new IllegalStateException("Future has already completed");
        }
        this.result = result;
        complete.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isCancelled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDone() {
        return (complete.getCount() == 0);
    }

    @Override
    public ExecutionResult<T> get() throws InterruptedException, ExecutionException {
        complete.await();
        return result;
    }

    @Override
    public ExecutionResult<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        complete.await(timeout, unit);
        return result;
    }
}
