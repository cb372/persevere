package com.github.cb372.persevere;

import com.github.cb372.persevere.action.GiveUp;
import com.github.cb372.persevere.action.RetryableAction;
import com.github.cb372.persevere.delay.DelayStrategies;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Author: chris
 * Created: 5/2/13
 */
public class PersevereTest {

    @Before
    public void initPersevere() {
        Persevere.init(2);
    }

    @After
    public void shutdownPersevere() {
        Persevere.shutdown();
    }

    @Test(timeout = 500)
    public void retriesAnActionUntilItSucceeds() throws ExecutionException, InterruptedException {
        RetryableAction<String> action = new RetryableAction<String>() {
            @Override
            public String execute(int retryCount) throws GiveUp, Exception {
                if (retryCount < 2) {
                    // fail on the first two attempts
                    throw new IOException("Argh!");
                }
                return "You can get it you really want";
            }
        };
        Future<ExecutionResult<String>> future = Persevere.persevere(action, DelayStrategies.retryImmediately(), 2);
        ExecutionResult<String> result = future.get();

        assertThat(result.success, is(true));
        assertThat(result.result, is("You can get it you really want"));
        assertThat(result.retries, is(2));
    }

    @Test(timeout = 1000, expected = CancellationException.class)
    public void canCancelAnActionWhileWaitingToRetry() throws ExecutionException, InterruptedException {
        RetryableAction<String> action = new RetryableAction<String>() {
            @Override
            public String execute(int retryCount) throws GiveUp, Exception {
                throw new IOException("Argh!");
            }
        };
        Future<ExecutionResult<String>> future = Persevere.persevere(action, DelayStrategies.fixedDelay(1000), 2);
        Thread.sleep(100);
        future.cancel(true);

        assertThat(future.isCancelled(), is(true));
        future.get(); // should throw exception
    }

    @Test(timeout = 1000, expected = CancellationException.class)
    public void canCancelAnActionWhileActionIsRunning() throws ExecutionException, InterruptedException {
        RetryableAction<String> action = new RetryableAction<String>() {
            @Override
            public String execute(int retryCount) throws GiveUp, Exception {
                Thread.sleep(1000); // will throw InterruptedException
                return "hello";
            }
        };
        Future<ExecutionResult<String>> future = Persevere.persevere(action, DelayStrategies.fixedDelay(1000), 2);
        Thread.sleep(100);
        future.cancel(true);

        assertThat(future.isCancelled(), is(true));
        future.get(); // should throw exception
    }
}
