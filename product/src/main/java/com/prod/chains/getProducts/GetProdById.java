package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.products.Product;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetProdById implements ChainHandler<ProductInfo> {
    private final IProductService productService;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()){
            Optional<Product> _product = productService.getProductById(
                    chainData.getValue().getProductId()
            );
            if (_product.isPresent()){
                ProductInfo dto = chainData.getValue();
                changeData(_product.get(), dto);
                chainData.setValue(dto)
                        .setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay san pham")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
    private void changeData(Product product, ProductInfo productInfo){
        productInfo.setProductId(product.getId());
        productInfo.setTitle(product.getTitle());
        productInfo.setPrice(product.getPrice());
        productInfo.setScore(product.getScore());
        productInfo.setReviews(product.getReview());
        productInfo.setDescription(product.getDescription());
    }
}
