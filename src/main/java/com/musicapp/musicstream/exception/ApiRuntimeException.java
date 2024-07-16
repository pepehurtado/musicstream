package com.musicapp.musicstream.exception;

public class ApiRuntimeException extends RuntimeException {

    private final int statusCode;
    private final String errorType; // Nuevo campo para el tipo de error

    public ApiRuntimeException(String message, int statusCode) {
        super(message);
        //Segun el status code, se asigna el tipo de error
        switch (statusCode) {
            case 400:
                this.errorType = "Bad Request";
                break;
            case 401:
                this.errorType = "Unauthorized";
                break;
            case 403:
                this.errorType = "Forbidden";
                break;
            case 404:
                this.errorType = "Not Found";
                break;
            case 500:
                this.errorType = "Internal Server Error";
                break;
            default:
                this.errorType = "Unknown Error";
                break;
        }
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorType() {
        return errorType;
    }
}
