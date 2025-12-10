package com.payment.custom;

// Tạo một ngoại lệ tùy chỉnh nếu cần
public class CustomClientException extends RuntimeException {
    public CustomClientException(String message) {
        super(message);
    }
}