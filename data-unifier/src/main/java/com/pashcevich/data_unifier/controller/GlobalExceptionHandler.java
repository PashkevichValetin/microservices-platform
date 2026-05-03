package com.pashcevich.data_unifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pashcevich.data_unifier.exception.AdapterException;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import com.pashcevich.data_unifier.exception.UserAdapterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(DataUnificationException.class)
    public ResponseEntity<Map<String, Object>> handleDataUnificationException(DataUnificationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), "DATA_UNIFICATION_FAILED", ex);
    }

    @ExceptionHandler(UserAdapterException.class)
    public ResponseEntity<Map<String, Object>> handleUserAdapterException(UserAdapterException ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "USER_ADAPTER_FAILED", ex);
    }


    @ExceptionHandler(AdapterException.class)
    public ResponseEntity<Map<String, Object>> handleAdapterException(AdapterException ex) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), "ADAPTER_ERROR", ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "RUNTIME_ERROR", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "INTERNAL_ERROR", ex);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, String errorCode, Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("path", Thread.currentThread().getName());

        return new ResponseEntity<>(errorResponse, status);
    }

}