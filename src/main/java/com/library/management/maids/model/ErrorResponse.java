package com.library.management.maids.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Data
@RequiredArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;
    private final String httpStatus;
    private final String refId;
    private final Instant timestamp;
}