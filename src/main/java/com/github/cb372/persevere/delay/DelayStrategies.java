package com.github.cb372.persevere.delay;

import java.util.Random;

/**
 * Author: chris
 * Created: 5/2/13
 */
public final class DelayStrategies {

    public static DelayStrategy fixedDelay(final long delayMs) {
        if (delayMs < 0) {
            throw new IllegalArgumentException("Delay must not be negative");
        }
        return new DelayStrategy() {
            @Override
            public long getNextDelayMs(int completedRetries) {
                return delayMs;
            }
        };
    }

    public static DelayStrategy retryImmediately() {
        return fixedDelay(0L);
    }

    public static DelayStrategy random(final int min, final int max) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        final Random random = new Random();
        return new DelayStrategy() {
            @Override
            public long getNextDelayMs(int completedRetries) {
                return min + random.nextInt(max - min);
            }
        };
    }

    public static DelayStrategy exponential(final int firstDelay, final double multiplier) {
        if (firstDelay <= 0) {
            throw new IllegalArgumentException("First delay must be greater than zero");
        }
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("Multiplier must be greater than or equal to 1.0");
        }
        return new DelayStrategy() {
            @Override
            public long getNextDelayMs(int completedRetries) {
                return (long) (firstDelay * Math.pow(multiplier, completedRetries));
            }
        };
    }

}
