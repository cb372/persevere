package com.github.cb372.persevere;

/**
 * The final result of performing an action, after possibly retrying one or more times.
 *
 * Author: chris
 * Created: 4/23/13
 */
public final class ExecutionResult<T> {
    /**
     * Did the action succeed (either on the first try or after one or more retries)
     */
    public final boolean success;

    /**
     * The result of the action. Will be null if the action did not succeed.
     */
    public final T result;

    /**
     * The exception thrown by the last attempt at performing the action. Will be null if the action succeeded.
     */
    public final Exception exception;

    /**
     * How many retries were performed. Zero means that the action succeeded on the first attempt.
     */
    public final int retries;

    private ExecutionResult(boolean success, T result, Exception exception, int retries) {
        this.success = success;
        this.result = result;
        this.exception = exception;
        this.retries = retries;
    }

    public static <T> ExecutionResult<T> success(T result, int retries) {
        // Note that null is a valid result, so we do not do a null check here
        return new ExecutionResult<T>(true, result, null, retries);
    }

    public static <T> ExecutionResult<T> failure(Exception exception, int retries) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception should not be null if the action failed");
        }
        return new ExecutionResult<T>(false, null, exception, retries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionResult that = (ExecutionResult) o;

        if (retries != that.retries) return false;
        if (success != that.success) return false;
        if (exception != null ? !exception.equals(that.exception) : that.exception != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = (success ? 1 : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (exception != null ? exception.hashCode() : 0);
        result1 = 31 * result1 + retries;
        return result1;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("Success after %d retries. Result: %s", retries, result);
        } else {
            return String.format("Failure after %d retries. Exception message: %s", retries, exception.getMessage());
        }
    }
}
