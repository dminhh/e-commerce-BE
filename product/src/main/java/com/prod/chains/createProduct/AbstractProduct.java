package com.prod.chains.createProduct;

import com.prod.facades.data.ColorSizeQuantityInfo;
import com.prod.facades.data.ProductInfo;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractProduct {
    public int getTotalQuantity(ProductInfo productInfo) {
        int sum = 0;
        for (ColorSizeQuantityInfo _csq : productInfo.getCsq()){
            sum += _csq.getQuantity();
        }
        return sum;
    }
}
