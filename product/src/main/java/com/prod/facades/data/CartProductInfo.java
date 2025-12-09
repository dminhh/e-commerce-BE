package com.prod.facades.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartProductInfo {
    private int quantity;
    private int productId;
    private int colorId;
    private int sizeId;
}
