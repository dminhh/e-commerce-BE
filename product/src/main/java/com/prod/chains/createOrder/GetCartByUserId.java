package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.carts.Cart;
import com.prod.services.carts.ICartService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetCartByUserId implements ChainHandler<CSPCartInfo> {
    private final ICartService cartService;

    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()) {
            CSPCartInfo dto = chainData.getValue();
            Optional<Cart> cart = cartService.getCartByUserId(chainData.getUserId());
            if (cart.isPresent()) {
                dto.setCart_id(cart.get().getId());
                chainData
                        .setValue(dto)
                        .setSuccess(true);
            } else {
                Cart newCart = cartService.createCart(
                        Cart.builder()
                                .user_id(chainData.getUserId())
                                .build()
                );
                dto.setCart_id(newCart.getId());
                chainData
                        .setValue(dto)
                        .setSuccess(true);
            }

        }
        return new Chain<>(this);
    }
}
