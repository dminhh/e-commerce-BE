package com.prod.chains.checkQuantityProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CartInfo;
import com.prod.models.carts.Cart_Product;
import com.prod.models.products.Product;
import com.prod.services.carts.ICartProductService;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@AllArgsConstructor
public class GetCartProductById implements ChainHandler<CartInfo> {
    private final IProductService productService;
    private final ICartProductService cartProductService;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if (!chainData.isSuccess()) {
            return new Chain<>(this);
        }
        Optional<Product> product = productService.getProductById(
                chainData.getValue().getProductId()
        );
        Optional<Cart_Product> cartProduct = cartProductService.getCartProductById(
                chainData.getValue().getCart_product_id()
        );
        if (product.isPresent() && cartProduct.isPresent()) {
            CartInfo dto = chainData.getValue();
            dto.setProduct(product.get().getTitle());
            dto.setPrice(product.get().getPrice());
            dto.setQuantity(cartProduct.get().getQuantity());
            chainData.setValue(dto).setSuccess(true);
        } else {
            chainData.setMessage("Khong tim thay san pham/gio hang").setSuccess(false);
        }
        return new Chain<>(this);
    }

}
