package com.musicapp.musicstream.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiRuntimeException.class)
    public ResponseEntity<ApiError> handleApiRuntimeException(ApiRuntimeException ex) {
        ApiError apiError = new ApiError(ex.getErrorType(), ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatusCode())).body(apiError);
    }
}
