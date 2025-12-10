package com.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Payment {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }
}