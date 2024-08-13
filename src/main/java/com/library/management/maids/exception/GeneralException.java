package com.library.management.maids.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;


public class GeneralException extends Exception{

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public GeneralException(HttpStatus httpStatus, String message, Throwable cause)
    {
        super(message,cause);
        this.status = httpStatus;
    }

    public GeneralException(HttpStatus httpStatus, String message)
    {
        super(message);
        this.status = httpStatus;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
