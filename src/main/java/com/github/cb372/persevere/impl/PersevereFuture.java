package com.github.cb372.persevere.impl;

import com.github.cb372.persevere.ExecutionResult;

import javax.annotation.concurrent.GuardedBy;
import java.util.concurrent.*;

/**
 * Author: chris
 * Created: 5/2/13
 */
public class PersevereFuture<T> implements Future<ExecutionResult<T>> {
    private final CountDownLatch complete = new CountDownLatch(1);
    private volatile ExecutionResult<T> result;
    @GuardedBy("this") private Future<?> currentTaskFuture;
    @GuardedBy("this") private boolean cancelled;

    /**
     * Mark the future as complete (either succeeded or failed).
     * After this method has been called, {@link #isDone()} will return true.
     *
     * @param result the result of performing the action
     */
    protected synchronized void markComplete(ExecutionResult<T> result) {
        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }
        if (!isDone()) {
            this.result = result;
            complete.countDown();
        }
    }

    protected synchronized void setCurrentTaskFuture(Future<?> currentTaskFuture) {
        this.currentTaskFuture = currentTaskFuture;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        } else {
            if (currentTaskFuture != null) {
                currentTaskFuture.cancel(mayInterruptIfRunning);
            }
            cancelled = true;
            complete.countDown();
            return true;
        }
    }

    @Override
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return (complete.getCount() == 0);
    }

    @Override
    public ExecutionResult<T> get() throws InterruptedException, ExecutionException {
        complete.await();
        if (isCancelled()) {
            throw new CancellationException();
        }
        return result;
    }

    @Override
    public ExecutionResult<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        complete.await(timeout, unit);
        if (isCancelled()) {
            throw new CancellationException();
        }
        return result;
    }
}
