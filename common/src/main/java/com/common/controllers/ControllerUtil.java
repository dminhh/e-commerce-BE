package com.common.controllers;

import com.common.DTO.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ControllerUtil {
    public static <T> ResponseEntity<Object> ok(T data) {
        return ResponseEntity.status((HttpStatus.OK)).body(ResponseObject.builder()
                .isSuccess(true)
                .data(data)
                .message("ok")
                .build());
    }

    public static <T> ResponseEntity<Object> notFound(String message) {
        return ResponseEntity.status((HttpStatus.NOT_FOUND))
                .body(ResponseObject.builder()
                        .isSuccess(false)
                        .data(null)
                        .message(message != null ? message : "Cannot find resources")
                        .build());
    }

    public static ResponseEntity<Object> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseObject.builder()
                        .isSuccess(false)
                        .data(null)
                        .message(message != null ? message : "Internal server error")
                        .build());
    }

    public static ResponseEntity<Object> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseObject.builder()
                        .isSuccess(false)
                        .data(null)
                        .message(message != null ? message : "Unauthorized")
                        .build());
    }

    public static ResponseEntity<Object> invalidated(Object errors,String message) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseObject.builder()
                        .isSuccess(false)
                        .data(errors)
                        .message(message != null ? message : "Validation errors")
                        .build());
    }
}
