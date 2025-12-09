package com.prod.chains.createProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.details.Season;
import com.prod.services.details.ISeasonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetSeasonById implements ChainHandler<ProductInfo>  {
    private final ISeasonService seasonService;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()){
            Optional<Season> _season = seasonService.getSeasonById(
                    chainData.getValue().getSeason_id()
            );
            if (_season.isEmpty())
                chainData
                        .setMessage("Khong tim thay mua tuong ung")
                        .setSuccess(false);
            else{
                ProductInfo dto = chainData.getValue();
                dto.setSeason_id(_season.get().getId());
                dto.setSeason(_season.get().getName() + " " + _season.get().getYear());
                chainData.setValue(dto).setSuccess(true);
            }
        }
        return new Chain<>(this);
    }
}
