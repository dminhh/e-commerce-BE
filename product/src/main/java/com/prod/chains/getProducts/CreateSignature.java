package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.products.Product;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@AllArgsConstructor
@Builder
public class CreateSignature implements ChainHandler<ProductInfo> {
    private final IProductService productService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            ProductInfo dto = chainData.getValue();
            List<String> sig = new ArrayList<>();
            Optional<Product> product = productService.getProductById(dto.getProductId());
            if (product.isPresent()) {
                initSignature(dto, sig);
                getSignature(dto, sig, product.get());
            }
        }
        return null;
    }

    private void initSignature(ProductInfo productInfo, List<String> sig) {
        sig.addAll(List.of(productInfo.getSeason().split(" ")));
        sig.addAll(List.of(productInfo.getCategoryName().split(" ")));
        sig.addAll(productInfo.getLabel());
    }

    private void getSignature(ProductInfo dto, List<String> sig, Product product) {
        Set<String> set = new LinkedHashSet<>();
        for (String s : sig) {
            set.add(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
        }
        String signature = String.join(" ", set);
        product.setSignature(signature);
        product.setUpdate_at(LocalDateTime.now());
        productService.createProduct(product);
    }
}
