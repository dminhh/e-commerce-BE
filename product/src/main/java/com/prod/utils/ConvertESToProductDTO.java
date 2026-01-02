package com.prod.utils;

import com.prod.facades.data.ImageInfo;
import com.prod.facades.data.ProductInfo;
import com.prod.models.elk.ESProducts;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConvertESToProductDTO {

    // Hàm của bạn (ProductInfo -> ESProducts)
    public ESProducts getES(ProductInfo dto){
        ESProducts products = new ESProducts();
        products.setDbId(dto.getProductId() + "");
        products.setTitle(dto.getTitle());
        products.setPrice(dto.getPrice());
        // Null check để tránh lỗi NullPointerException
        if (dto.getImages() != null) {
            products.setImages(dto.getImages().stream()
                    .map(ImageInfo::getUrl)
                    .collect(Collectors.toList()));
        }
        products.setDiscount(0);
        products.setRating(dto.getScore());
        products.setSold(dto.getSold());
        return products;
    }
    public ProductInfo toProductInfo(ESProducts es) {
        ProductInfo dto = new ProductInfo();
        try {
            if (es.getDbId() != null) {
                dto.setProductId(Integer.parseInt(es.getDbId()));
            }
        } catch (NumberFormatException e) {
            dto.setProductId(0);
        }
        dto.setTitle(es.getTitle());
        dto.setPrice((long) es.getPrice());
        dto.setScore(es.getScore());
        dto.setSold(es.getSold());
        dto.setReviews(0);
        dto.setDescription("");
        if (es.getImages() != null && !es.getImages().isEmpty()) {
            List<ImageInfo> imageInfos = es.getImages().stream().map(url -> {
                ImageInfo img = new ImageInfo();
                img.setUrl(url);
                return img;
            }).collect(Collectors.toList());

            dto.setImages(imageInfos);
        } else {
            dto.setImages(new ArrayList<>());
        }

        return dto;
    }

}