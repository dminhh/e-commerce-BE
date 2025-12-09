package com.prod.facades.data;

import com.prod.validators.LabelValid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInfo {
    //product
    private int productId;
    private String title;
    private long price;
    @Builder.Default
    private int reviews = 0;
    @Builder.Default
    private double score = 0.0;
    private int quantity;
    private int sold;
    private String description;
    //label
    @LabelValid
    private List<String> label;
    //category
    private int category;
    private String categoryName;
    //season
    private int season_id;
    private String season;
    //image
    private List<ImageInfo> images;
    //color size quantity
    private List<ColorSizeQuantityInfo> csq;
}
