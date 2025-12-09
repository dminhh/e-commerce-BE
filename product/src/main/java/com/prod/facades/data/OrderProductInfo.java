package com.prod.facades.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductInfo {
    //cart
    @JsonProperty("csp_id")
    private int csp_id;
    //product
    @JsonProperty("product_id")
    private int product_id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("image")
    private String image;
    @JsonProperty("quantity")
    private int quantity;
    private boolean first_review;
    @JsonProperty("price")
    //gia cua 1 san pham
    private long price;
    @JsonProperty("color")
    private String color;
    @JsonProperty("size")
    private String size;
}
