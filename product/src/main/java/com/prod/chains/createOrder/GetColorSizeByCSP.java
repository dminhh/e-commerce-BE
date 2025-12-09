package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.carts.Color;
import com.prod.models.carts.Color_Size_Product;
import com.prod.models.carts.Size;
import com.prod.services.carts.IColorService;
import com.prod.services.carts.IColorSizeProductService;
import com.prod.services.carts.ISizeService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
public class GetColorSizeByCSP implements ChainHandler<CSPCartInfo> {
    private final IColorSizeProductService cspService;
    private final IColorService colorService;
    private final ISizeService sizeService;

    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()) {
            CSPCartInfo dto = chainData.getValue();
            Optional<Color_Size_Product> csp = cspService.getColorSizeProductById(
                    dto.getCsp_id()
            );
            if (csp.isPresent()) {
                Optional<Color> color = colorService.getColorById(csp.get().getColor_id());
                Optional<Size> size = sizeService.getSizeById(csp.get().getSize_id());
                if (color.isPresent() && size.isPresent()) {
                    dto.setColor(color.get().getValue());
                    dto.setSize(size.get().getValue());
                    chainData.setSuccess(true);
                } else {
                    chainData.setMessage("Khong tim thay kich thuoc va mau").setSuccess(false);
                }

            }
        }
        return null;
    }
}
