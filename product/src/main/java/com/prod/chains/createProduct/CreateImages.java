package com.prod.chains.createProduct;

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
public class CreateImages implements ChainHandler<ProductInfo> {
    private final IImageService imageService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            ProductInfo dto = chainData.getValue();
            List<Image> images = imageService.getImagesByProductId(dto.getProductId());
            List<ImageInfo> res = new ArrayList<>();
            if (!images.isEmpty()) {
                imageService.deleteImageByProductId(dto.getProductId());
            }
            chainData.getValue().getImages().forEach(
                    i -> {
                        res.add(createImage(i, dto.getProductId()));
                    }
            );
            chainData.setSuccess(true);
//            else {
//                for (Image image : images) {
//                    for (ImageDTO imageDTO : dto.getImages()) {
//                        if (imageDTO.getName().equals(image.getName()) &&
//                                imageDTO.getTypeImage().equals(image.getType())) {
//                            res.add(updateImage(image, imageDTO.getUrl()));
//                        } else {
//                            res.add(createImage(imageDTO, dto.getProductId()));
//                        }
//                        images.remove(image);
//                        dto.getImages().remove(imageDTO);
//                    }
//                }
//            }
            if (!res.isEmpty()) {
                dto.setImages(res);
                chainData.setValue(dto).setSuccess(true);
            } else {
                chainData.setMessage("Khong the them hinh anh, danh sach trong")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }

    private ImageInfo createImage(ImageInfo dto, int productId){
        Image newImage = imageService.createImage(
                Image.builder()
                        .name(dto.getName())
                        .src(dto.getUrl())
                        .type(dto.getTypeImage())
                        .product_id(productId)
                        .build()
        );
        return ImageInfo.builder()
                .url(newImage.getSrc())
                .name(newImage.getName())
                .typeImage(newImage.getType())
                .build();
    }

//    private ImageDTO updateImage(Image image, String src){
//        image.setSrc(src);
//        image.setUpdate_at(LocalDateTime.now());
//        Image newImage = imageService.createImage(image);
//        return ImageDTO.builder()
//                .url(newImage.getSrc())
//                .name(newImage.getName())
//                .typeImage(newImage.getType())
//                .build();
//    }
}
