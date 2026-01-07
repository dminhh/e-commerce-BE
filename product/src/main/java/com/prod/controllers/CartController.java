package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.ICartFacade;
import com.prod.facades.data.CartInfo;
import com.prod.facades.data.CartProductInfo;
import com.prod.redis.AccountRedis;
import com.prod.redis.UserRedis;
import com.prod.services.elk.impl.ESProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController extends Controller<CartInfo> {
    @Autowired
    private ICartFacade cartFacade;

    @Autowired
    private ESProductService esProductService;

    @GetMapping("/getCart")
    public ResponseEntity<ResponseObject<Page<CartInfo>>> getCart(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            AccountRedis accountRedis = getAccount(request);
            if (userRedis == null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<CartInfo>>builder()
                                .message(NOT_FOUND_USER)
                                .build()
                );
            } else {
                ResponseObject<Page<CartInfo>> res = cartFacade.getCartByUserId(userRedis.getId(), accountRedis, page, size);
                return ResponseEntity.ok().body(res);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorsP();
        }
    }

    private ResponseEntity<ResponseObject<Page<CartInfo>>> serverErrorsP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<CartInfo>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<Page<CartInfo>>> updateCart(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestBody List<CartProductInfo> cartProductInfo,
                                                                     HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<CartInfo>>builder()
                                .message(NOT_FOUND_USER)
                                .build()
                );
            } else {
                try {
                    for (CartProductInfo productDTO : cartProductInfo) {
                        String id = productDTO.getProductId() + "";
                        esProductService.updateScore(id, 3);
                    }
                } catch (Exception ignored) {
                    log.error(ignored.getMessage());
                }
                return ResponseEntity.ok().body(cartFacade.updateCartByUserId(cartProductInfo, userRedis.getId(), page, size));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorsP();
        }
    }

    @Override
    public ResponseEntity<ResponseObject<List<CartInfo>>> notOwners() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject<List<CartInfo>>> serverErrors() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<List<CartInfo>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

    private ResponseEntity<ResponseObject<List<CartInfo>>> notFoundUser() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<List<CartInfo>>builder()
                        .message(NOT_FOUND_USER)
                        .build()
        );
    }
    @DeleteMapping("/delete/{cartProductId}")
    public ResponseEntity<ResponseObject<Page<CartInfo>>> deleteCartProduct(
            @PathVariable int cartProductId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<CartInfo>>builder()
                                .message(NOT_FOUND_USER)
                                .build()
                );
            } else {
                ResponseObject<Page<CartInfo>> res = cartFacade.deleteCartProduct(cartProductId, userRedis.getId(), page, size);
                return ResponseEntity.ok().body(res);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorsP();
        }
    }
    @PutMapping("/update-quantity/{cartProductId}")
    public ResponseEntity<ResponseObject<Page<CartInfo>>> updateQuantity(
            @PathVariable int cartProductId,
            @RequestParam int quantity,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<CartInfo>>builder()
                                .message(NOT_FOUND_USER)
                                .build()
                );
            } else {
                ResponseObject<Page<CartInfo>> res = cartFacade.updateCartProductQuantity(cartProductId, quantity, userRedis.getId(), page, size);
                return ResponseEntity.ok().body(res);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorsP();
        }
    }
}
