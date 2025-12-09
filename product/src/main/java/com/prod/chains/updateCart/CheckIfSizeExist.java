package com.prod.chains.updateCart;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CartInfo;
import com.prod.models.carts.Size;
import com.prod.services.carts.ISizeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CheckIfSizeExist implements ChainHandler<CartInfo> {
    private final ISizeService sizeService;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if (chainData.isSuccess()){
            Optional<Size> size = sizeService.getSizeById(chainData.getValue().getSizeId());
            if (size.isPresent()){
                CartInfo dto = chainData.getValue();
                dto.setSize(size.get().getValue());
                chainData.setValue(dto);
                chainData.setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay size san pham")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
