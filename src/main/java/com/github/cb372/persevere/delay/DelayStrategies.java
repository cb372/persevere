package com.github.cb372.persevere.delay;

/**
 * Author: chris
 * Created: 5/2/13
 */
public final class DelayStrategies {

    public static DelayStrategy retryImmediately() {
        return new DelayStrategy() {
            @Override
            public long getNextDelayMs(int completedRetries) {
                return 0;
            }
        };
    }

    public static DelayStrategy fixed(final long delayMs) {
        return new DelayStrategy() {
            @Override
            public long getNextDelayMs(int completedRetries) {
                return delayMs;
            }
        };
    }

    // TODO random, exponential
}
