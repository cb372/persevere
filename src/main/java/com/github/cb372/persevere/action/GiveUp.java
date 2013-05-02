package com.github.cb372.persevere.action;

/**
 * Exception thrown by a {@link RetryableAction} to indicate that
 * it has failed and should not be retried.
 *
 * Author: chris
 * Created: 5/2/13
 */
public final class GiveUp extends Exception {

    public GiveUp(String message) {
        super(message);
    }

    public GiveUp(String message, Exception cause) {
        super(message, cause);
    }

    public static GiveUp giveUp() {
        return new GiveUp("Gave up!");
    }

    public static GiveUp giveUp(String message) {
        return new GiveUp(message);
    }

    public static GiveUp giveUpBecauseOf(Exception cause) {
        return new GiveUp("Gave up because of " + cause.getClass().getSimpleName(), cause);
    }


}
