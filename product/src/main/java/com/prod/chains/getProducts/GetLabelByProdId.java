package com.prod.chains.getProducts;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.products.Label;
import com.prod.models.products.Label_Product;
import com.prod.services.products.ILabelProductService;
import com.prod.services.products.ILabelService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
public class GetLabelByProdId implements ChainHandler<ProductInfo> {
    private final ILabelService labelService;
    private final ILabelProductService labelProductService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            List<Label_Product> _label = labelProductService.getLabelProductsByProductId(
                    chainData.getValue().getProductId()
            );
            if (!_label.isEmpty()) {
                List<String> label = new ArrayList<>();
                for (Label_Product label_product : _label) {
                    Optional<Label> labelOptional = labelService.getLabelById(label_product.getLabel_id());
                    labelOptional.ifPresent(value -> label.add(value.getName()));
                }
                ProductInfo dto = chainData.getValue();
                changeData(dto, label);
                chainData
                        .setValue(dto)
                        .setSuccess(true);
            } else chainData
                    .setMessage("Khong tim thay label")
                    .setSuccess(false);
        }
        return new Chain<>(this);
    }

    public void changeData(ProductInfo dto, List<String> labels) {
        dto.setLabel(labels);
    }
}
