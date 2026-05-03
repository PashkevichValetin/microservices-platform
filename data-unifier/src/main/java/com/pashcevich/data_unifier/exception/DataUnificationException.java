package com.pashcevich.data_unifier.exception;

public class DataUnificationException extends RuntimeException {
    public DataUnificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataUnificationException(String message) {
        super(message);
    }
}