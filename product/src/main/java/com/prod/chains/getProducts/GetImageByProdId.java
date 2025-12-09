package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ImageInfo;
import com.prod.facades.data.ProductInfo;
import com.prod.models.products.Image;
import com.prod.services.products.IImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class GetImageByProdId implements ChainHandler<ProductInfo> {
    private final IImageService imageService;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()){
            List<Image> images = imageService.getImagesByProductId(
                    chainData.getValue().getProductId()
            );
            if (!images.isEmpty()){
                ProductInfo dto = chainData.getValue();
                changeData(dto, images);
                chainData.setValue(dto)
                        .setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay danh sach anh cua san pham")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
    private void changeData(ProductInfo productInfo, List<Image> images){
        List<ImageInfo> imageInfos = new ArrayList<>();
        images.forEach(image -> {
            imageInfos.add(ImageInfo.builder()
                            .url(image.getSrc())
                            .name(image.getName())
                            .typeImage(image.getType())
                    .build());
        });
        productInfo.setImages(imageInfos);
    }
}
