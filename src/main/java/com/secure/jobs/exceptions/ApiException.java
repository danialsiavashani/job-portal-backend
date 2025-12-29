package com.secure.jobs.exceptions;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return status;
    }
}

//Why abstract?. Forces you to create intentional exceptions and No random throw new RuntimeException()