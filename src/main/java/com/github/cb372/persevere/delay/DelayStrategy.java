package com.github.cb372.persevere.delay;

/**
 * Author: chris
 * Created: 4/22/13
 */
public interface DelayStrategy {

    /**
     * Choose the required length of time to pause after the given retry
     *
     * @param completedRetries
     *  How many retries have been completed. e.g. 0 -> the pause between the first try and the first retry.
     * @return required delay in ms
     */
    public long getNextDelayMs(int completedRetries);

}
