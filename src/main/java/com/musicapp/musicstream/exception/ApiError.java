package com.musicapp.musicstream.exception;

import lombok.Data;

@Data
public class ApiError {
    private String status;
    private int statusCode;
    private String description;

    public ApiError(String status, int statusCode, String description) {
        this.status = status;
        this.statusCode = statusCode;
        this.description = description;
    }
}
