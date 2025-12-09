package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.orders.Order_Product;
import com.prod.services.carts.ICartProductService;
import com.prod.services.orders.IOrderProductService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Builder
public class CreateOrderProductIfTrue implements ChainHandler<CSPCartInfo> {
    private final IOrderProductService orderProductService;
    private final ICartProductService cartProductService;

    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()) {
            CSPCartInfo dto = chainData.getValue();
            Order_Product newOrderProduct = orderProductService.createOrderProduct(
                    Order_Product.builder()
                            .order_id(dto.getOrder_id())
                            .csp_id(dto.getCsp_id())
                            .quantity(dto.getQuantity())
                            .total(dto.getQuantity() * dto.getPrice())
                            .build()
            );
            //xoa san pham trong cart product neu xuat hien
            cartProductService.getCartProductByCartAndCSP(
                    dto.getCart_id(), newOrderProduct.getCsp_id()
            ).ifPresent(cartProduct -> {
                cartProductService.deleteCartProduct(cartProduct.getId());
            });

        }
        return new Chain<>(this);
    }
}
