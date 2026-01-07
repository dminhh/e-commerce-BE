package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.CartInfo;
import com.prod.facades.data.CartProductInfo;
import com.prod.models.carts.Color_Size_Product;
import com.prod.redis.AccountRedis;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ICartFacade {
    ResponseObject<Page<CartInfo>> getCartByUserId(int userId, AccountRedis accountRedis, int page, int size);
    ResponseObject<Page<Color_Size_Product>> getCSP(int cartId, int page, int size, String field, String direct);
    ResponseObject<Page<CartInfo>> updateCartByUserId(List<CartProductInfo> cartProductInfo, int userId, int page, int size);
    ResponseObject<Page<CartInfo>> deleteCartProduct(int cartProductId, int userId, int page, int size);

    // Cập nhật quantity
    ResponseObject<Page<CartInfo>> updateCartProductQuantity(int cartProductId, int quantity, int userId, int page, int size);
}
