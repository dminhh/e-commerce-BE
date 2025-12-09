package com.prod.chains.updateCart;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CartInfo;
import com.prod.models.carts.Color;
import com.prod.services.carts.IColorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CheckIfColorExist implements ChainHandler<CartInfo> {
    private final IColorService colorService;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if (chainData.isSuccess()){
            Optional<Color> color = colorService.getColorById(chainData.getValue().getColorId());
            if (color.isPresent()){
                CartInfo dto = chainData.getValue();
                dto.setColors(color.get().getValue());
                dto.setCode(color.get().getCode());
                chainData.setValue(dto).setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay mau").setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
