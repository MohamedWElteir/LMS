package com.library.management.maids.exception.handler;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.ErrorResponse;
import com.library.management.maids.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class UnifiedExceptionHandler {

    private final MessageSource messageSource;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.VALIDATION_ERROR,  ex.getMessage(), HttpStatus.BAD_REQUEST.name(), request.getSessionId(), Instant.now());

        return new ResponseEntity<>(errorResponse, createHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException ex, WebRequest request) {
        String code = ex.getMessage();
        String message = messageSource.getMessage(code, null, null, Locale.getDefault());
        ErrorResponse errorResponse = new ErrorResponse(code, message, ex.getStatus().name(), request.getSessionId(), Instant.now());

        return new ResponseEntity<>(errorResponse, createHeaders(), ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                "INTERNAL_SERVER_ERROR",
                request.getSessionId(),
                Instant.now()
        );

        log.error("Unexpected exception happened", ex);
        return new ResponseEntity<>(errorResponse, createHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
