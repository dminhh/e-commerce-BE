package com.prod.utils;

import com.prod.facades.data.ImageInfo;
import com.prod.facades.data.ProductInfo;
import com.prod.models.elk.ESProducts;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class ConvertESToProductDTO {
    public ESProducts getES(ProductInfo dto){
        ESProducts products = new ESProducts();
        products.setDbId(dto.getProductId() + "");
        products.setTitle(dto.getTitle());
        products.setPrice(dto.getPrice());
        products.setImages(dto.getImages().stream()
                .map(ImageInfo::getUrl)
                .collect(Collectors.toList()));
        products.setDiscount(0);
        products.setRating(dto.getScore());
        products.setSold(dto.getSold());
        return products;
    }
}
