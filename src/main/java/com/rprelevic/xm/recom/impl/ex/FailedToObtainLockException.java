package com.rprelevic.xm.recom.impl.ex;

/**
 * Exception thrown when failed to obtain a lock.
 */
public class FailedToObtainLockException extends RuntimeException {

    public FailedToObtainLockException(String message) {
        super(message);
    }
}
