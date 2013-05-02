package com.github.cb372.persevere.impl;


import com.github.cb372.persevere.ExecutionResult;
import com.github.cb372.persevere.action.RetryableAction;
import com.github.cb372.persevere.delay.DelayStrategies;
import com.github.cb372.persevere.delay.DelayStrategy;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.cb372.persevere.action.GiveUp.giveUp;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Author: chris
 * Created: 5/2/13
 */
@SuppressWarnings("unchecked") // caused my mocking generic classes
public class PersevereTaskTest {
    private final int maxRetries = 5;
    private final PersevereFuture<String> future = mock(PersevereFuture.class);
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final Exception exception = new RuntimeException("yelp!");

    @Test
    public void actionSucceeds_passesASuccessfulResultToFuture() throws Exception {
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(0)).thenReturn("hello");

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, 0);
        task.run();

        verify(future).markComplete(ExecutionResult.success("hello", 0));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void actionThrowsException_schedulesARetry() throws Exception {
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(0)).thenThrow(exception);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, 0);
        task.run();

        verify(future, never()).markComplete(any(ExecutionResult.class));
        verify(executor).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void actionThrowsGiveUp_passesAFailureResultToFutureAndGivesUp() throws Exception {
        RetryableAction<String> action = mock(RetryableAction.class);
        Exception giveUp = giveUp();
        when(action.execute(0)).thenThrow(giveUp);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, 0);
        task.run();

        verify(future).markComplete(ExecutionResult.<String>failure(giveUp, 0));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void actionSucceedsOnItsLastRetry_passesASuccessfulResultToFuture() throws Exception {
        final int retryCount = maxRetries;
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(retryCount)).thenReturn("hello");

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, retryCount);
        task.run();

        verify(future).markComplete(ExecutionResult.success("hello", 5));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void actionThrowsExceptionOnItsLastRetry_passesAFailureResultToFuture() throws Exception {
        final int retryCount = maxRetries;
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(retryCount)).thenThrow(exception);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, retryCount);
        task.run();

        verify(future).markComplete(ExecutionResult.<String>failure(exception, retryCount));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void usesDelayStrategyToChooseDelay() throws Exception {
        final int retryCount = 3;

        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(retryCount)).thenThrow(exception);

        DelayStrategy delayStrategy = mock(DelayStrategy.class);
        when(delayStrategy.getNextDelayMs(retryCount)).thenReturn(300L);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetries, delayStrategy);
        PersevereTask<String> task = new PersevereTask<String>(config, retryCount);
        task.run();

        verify(delayStrategy).getNextDelayMs(3);
        verify(executor).schedule(any(Runnable.class), eq(300L), any(TimeUnit.class));
    }

    @Test
    public void retryCountIsZero_doesNotRetry() throws Exception {
        int maxRetriesZero = 0;
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(0)).thenThrow(exception);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetriesZero, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, 0);
        task.run();

        verify(future).markComplete(ExecutionResult.<String>failure(exception, 0));
        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void retryCountIsNegative_schedulesARetry() throws Exception {
        int maxRetriesInfinite = -123;
        int retryCount = 10000;
        RetryableAction<String> action = mock(RetryableAction.class);
        when(action.execute(retryCount)).thenThrow(exception);

        PersevereTask.Config<String> config = new PersevereTask.Config<String>(action, future, executor, maxRetriesInfinite, DelayStrategies.retryImmediately());
        PersevereTask<String> task = new PersevereTask<String>(config, retryCount);
        task.run();

        verify(future, never()).markComplete(any(ExecutionResult.class));
        verify(executor).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
    }
}
