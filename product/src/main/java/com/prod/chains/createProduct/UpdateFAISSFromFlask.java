package com.prod.chains.createProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.facades.flaskAPIs.UpdateFAISSIndex;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateFAISSFromFlask implements ChainHandler<ProductInfo> {
    private final UpdateFAISSIndex updateFAISSIndex;
    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()){
            ProductInfo info = chainData.getValue();
            updateFAISSIndex.updateIndex(info.getProductId(), info.getTitle());
        }
        return new Chain<>(this);
    }
}
