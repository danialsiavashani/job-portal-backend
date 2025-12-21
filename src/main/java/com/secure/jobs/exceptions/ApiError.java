package com.secure.jobs.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ApiError(int status, String error, String message, String path){
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

}
