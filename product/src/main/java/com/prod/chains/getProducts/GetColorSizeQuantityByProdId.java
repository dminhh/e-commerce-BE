package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ColorSizeQuantityInfo;
import com.prod.facades.data.ProductInfo;
import com.prod.models.carts.Color;
import com.prod.models.carts.Color_Size_Product;
import com.prod.models.carts.Size;
import com.prod.models.carts.Small_Quantity;
import com.prod.services.carts.IColorService;
import com.prod.services.carts.IColorSizeProductService;
import com.prod.services.carts.ISizeService;
import com.prod.services.carts.ISmallQuantityService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
@Builder
public class GetColorSizeQuantityByProdId implements ChainHandler<ProductInfo> {
    private final IColorSizeProductService cspService;
    private final ISmallQuantityService smallQuantityService;
    private final IColorService colorService;
    private final ISizeService sizeService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            List<Color_Size_Product> csps = cspService.getColorSizeProductsByProductId(
                    chainData.getValue().getProductId()
            );
            ProductInfo productInfo = chainData.getValue();
            if (csps != null) {
                csps.forEach(csp -> {
                    changeData(productInfo, csp);
                });
                chainData.setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay color size product")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }

    private void changeData(ProductInfo productInfo, Color_Size_Product csp) {
        ColorSizeQuantityInfo csqDTO = new ColorSizeQuantityInfo();
        //quantity
        Optional<Small_Quantity> quantity = smallQuantityService.getByCSProductId(csp.getId());
        quantity.ifPresent(smallQuantity -> {
            csqDTO.setQuantity(smallQuantity.getQuantity());
            csqDTO.setSold(smallQuantity.getSold());
        });
        //color
        Optional<Color> color = colorService.getColorById(csp.getColor_id());
        color.ifPresent(c -> {
            csqDTO.setColor(c.getValue());
            csqDTO.setCode(c.getCode());
            csqDTO.setColor_id(c.getId());
        });
        //size
        Optional<Size> size = sizeService.getSizeById(csp.getSize_id());
        size.ifPresent(s -> {
            csqDTO.setSize(s.getValue());
            csqDTO.setSize_id(s.getId());
        });
        //csp
        csqDTO.setCsq_id(csp.getId());
        //
        List<ColorSizeQuantityInfo> csqList = productInfo.getCsq();
        if (csqList == null) {
            csqList = new ArrayList<>();
        }
        csqList.add(csqDTO);
        productInfo.setCsq(csqList);
    }
}
