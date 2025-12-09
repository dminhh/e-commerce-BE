package com.prod.chains.updateCart;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CartInfo;
import com.prod.models.ENUM.Type_Image;
import com.prod.models.products.Image;
import com.prod.services.products.IImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetFirstImageByProdId implements ChainHandler<CartInfo> {
    private final IImageService imageService;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if (chainData.isSuccess()){
            CartInfo dto = chainData.getValue();
            Optional<Image> image = imageService.getImageByProdIdAndType(dto.getProductId(), Type_Image.ANH_NEN);
            if (image.isPresent()) {
                dto.setImageSrc(image.get().getSrc());
                chainData.setValue(dto).setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay anh nen").setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
