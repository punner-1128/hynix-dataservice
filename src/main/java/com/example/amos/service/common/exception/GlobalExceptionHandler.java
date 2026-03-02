package com.example.amos.service.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.amos.service.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleServiceException(ServiceException ex) {
        Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("reason", ex.getMessage());

        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getCode(), ex.getMessage(), errorData));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleException(Exception ex) {
        Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("reason", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("9999", "Internal Server Error", errorData));
    }
}
