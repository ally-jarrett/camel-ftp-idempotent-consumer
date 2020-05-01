package com.redhat.summit.exception;

public class CustomException extends Exception {
    public CustomException(String errorMessage) {
        super(errorMessage);
    }
}
