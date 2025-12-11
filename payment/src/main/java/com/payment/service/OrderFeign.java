package com.payment.service;

import com.payment.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "OrderFeign", url = "http://localhost:7081", configuration = FeignConfig.class)
public interface OrderFeign {
    @GetMapping("/api/order/paid?id={id}")
    ResponseEntity<Object> paySusses( @PathVariable("id") int id);
//    @GetMapping("/api/order/change-status??id={id}")//http://localhost:7081/api/order/change-status?id=53
//    ResponseEntity<Object> changeOrder( @PathVariable("id") int id);
}
