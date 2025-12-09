package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.ENUM.Type_Image;
import com.prod.models.products.Image;
import com.prod.models.products.Product;
import com.prod.services.products.IImageService;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
public class GetProductById implements ChainHandler<CSPCartInfo> {
    private final IProductService productService;
    private final IImageService imageService;
    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()){
            CSPCartInfo dto = chainData.getValue();
            Optional<Product> product = productService.getProductById(dto.getProduct_id());
            if (product.isPresent()){
                Optional<Image> image = imageService.getImageByProdIdAndType(product.get().getId(), Type_Image.ANH_NEN);
                dto.setPrice(product.get().getPrice());
                dto.setName(product.get().getTitle());
                image.ifPresent(i -> dto.setImage(i.getSrc()));
                chainData
                        .setValue(dto)
                        .setSuccess(true);
            } else {
                chainData
                        .setMessage("Khong tim thay san pham")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
