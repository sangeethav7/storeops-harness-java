package com.storeops.common.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        Map<String, Object> body = Map.of(
            "errorCode", ex.getErrorCode(),
            "message", ex.getMessage()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }
}
