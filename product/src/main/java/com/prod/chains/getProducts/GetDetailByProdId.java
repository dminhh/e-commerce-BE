package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.details.Detail;
import com.prod.models.details.Quantity;
import com.prod.services.details.IDetailService;
import com.prod.services.details.IQuantityService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Builder
@AllArgsConstructor
public class GetDetailByProdId implements ChainHandler<ProductInfo> {
    private final IDetailService detailService;
    private final IQuantityService quantityService;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()){
            Optional<Detail> _detail = detailService.getDetailByProductId(
                    chainData.getValue().getProductId()
            );
            if (_detail.isPresent()){
                ProductInfo dto = chainData.getValue();
                changeData(dto, _detail.get());
                chainData.setValue(dto)
                        .setSuccess(true);
            }
            else {
                chainData.setMessage("Khong tim thay detail")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
    private void changeData(ProductInfo dto, Detail detail){
        dto.setCategory(detail.getCategory_id());
        dto.setSeason_id(detail.getSeason_id());
        Optional<Quantity> quantity = quantityService.getQuantityById(detail.getQuantity_id());
        if (quantity.isPresent()){
            dto.setQuantity(quantity.get().getQuantity());
            dto.setSold(quantity.get().getSold());
        }
    }
}
