package com.prod.chains.checkQuantityProduct;

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
public class GetSizeById implements ChainHandler<CartInfo> {
    private final ISizeService sizeService;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if(!chainData.isSuccess()) {
            return new Chain<>(this);
        }
        int size_id = chainData.getValue().getSizeId();
        Optional<Size> size = sizeService.getSizeById(size_id);
        if (size.isPresent()) {
            CartInfo dto = chainData.getValue();
            dto.setSize(size.get().getValue());
            chainData.setValue(dto).setSuccess(true);
            return new Chain<>(this);
        } else {
            chainData.setMessage("Khong tim thay size san pham");
            chainData.setSuccess(false);
            return new Chain<>(this);
        }
    }
}
