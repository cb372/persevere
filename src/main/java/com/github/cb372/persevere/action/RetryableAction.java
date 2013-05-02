package com.github.cb372.persevere.action;

/**
 * Author: chris
 * Created: 4/22/13
 */
public interface RetryableAction<T> {

    /**
     * Perform some action and return a result.
     *
     * <ul>
     *   <li>If the action succeeds, it should return a result of type T.</li>
     *   <li>If it fails, it should throw an exception. It will be retried, unless all retries have already been used.</li>
     *   <li>If it fails and should not be retried, it should throw {@link GiveUp}.</li>
     * </ul>
     *
     * @param retryCount
     *  How many times the action has been retried. This will be 0 on the first try, 1 on the first retry, etc.
     * @return result
     * @throws Exception
     */
    public T execute(int retryCount) throws GiveUp, Exception;

}
