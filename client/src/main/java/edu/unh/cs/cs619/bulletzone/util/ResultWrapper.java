package edu.unh.cs.cs619.bulletzone.util;

import java.io.Serializable;

/**
 * Created by simon on 10/1/14.
 */
public class ResultWrapper<T> implements Serializable {
    private boolean success;
    private String message;
    private T result;

    public ResultWrapper() {
    }

    public ResultWrapper(boolean success, String message, T result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return this.result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    // Convenience method for getting user ID when T is Long
    public Long getUserId() {
        return (result instanceof Long) ? (Long) result : null;
    }
}