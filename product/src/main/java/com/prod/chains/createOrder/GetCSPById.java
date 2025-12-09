package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.carts.Color_Size_Product;
import com.prod.services.carts.IColorSizeProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetCSPById implements ChainHandler<CSPCartInfo> {
    private final IColorSizeProductService colorSizeProductService;
    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()) {
            CSPCartInfo dto = chainData.getValue();
            Optional<Color_Size_Product> csp = colorSizeProductService.getColorSizeProductById(dto.getCsp_id());
            if (csp.isPresent()) {
                dto.setProduct_id(csp.get().getProduct_id());
                chainData
                        .setValue(dto)
                        .setSuccess(true);
            } else {
                chainData
                        .setMessage("Khong tim thay san pham trong kho")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
