package com.prod.chains.createProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.details.Detail;
import com.prod.models.details.Quantity;
import com.prod.models.products.Product;
import com.prod.services.details.IDetailService;
import com.prod.services.details.IQuantityService;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Builder
public class CreateProduct extends AbstractProduct implements ChainHandler<ProductInfo>  {
    private final IProductService productService;
    private final IDetailService detailService;
    private final IQuantityService quantityService;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (!chainData.isSuccess())
            return new Chain<>(this);
        Product product = Product.builder()
                .description(chainData.getValue().getDescription())
                .title(chainData.getValue().getTitle())
                .price(chainData.getValue().getPrice())
                .build();
        Product _product = productService.createProduct(product);
        if(_product == null) {
            chainData.setMessage("Khong the tao san pham")
                    .setSuccess(false);
        } else {
            Quantity quantity = createQuantity(chainData.getValue());
            Detail detail = createDetail(product.getId(), quantity.getId(), quantity.getSold(), chainData.getValue());
            ProductInfo dto = changeData(chainData.getValue(), product, quantity.getQuantity(), quantity.getSold());
            chainData.setValue(dto);
            chainData.setSuccess(true);
        }
        return new Chain<>(this);
    }

    private Quantity createQuantity(ProductInfo productInfo){
        return quantityService.createQuantity(Quantity.builder()
                .quantity(getTotalQuantity(productInfo))
                .build());
    }
    private Detail createDetail(int pId, int qId, int sold, ProductInfo productInfo){
        return detailService.createDetail(Detail.builder()
                .product_id(pId)
                .category_id(productInfo.getCategory())
                .quantity_id(qId)
                .season_id(productInfo.getSeason_id())
                .build());
    }
    private ProductInfo changeData(ProductInfo dto, Product _product, int qtt, int sold){
        dto.setProductId(_product.getId());
        dto.setTitle(_product.getTitle());
        dto.setPrice(_product.getPrice());
        dto.setSold(sold);
        dto.setQuantity(qtt);
        return dto;
    }
}
