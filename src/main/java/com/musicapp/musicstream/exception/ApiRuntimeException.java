package com.musicapp.musicstream.exception;

public class ApiRuntimeException extends RuntimeException {

    private final int statusCode;

    public ApiRuntimeException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
