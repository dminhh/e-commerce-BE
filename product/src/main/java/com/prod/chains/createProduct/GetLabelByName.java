package com.prod.chains.createProduct;

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
public class GetLabelByName implements ChainHandler<ProductInfo> {
    private final ILabelService labelService;
    private final ILabelProductService labelProductService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            try {
                ProductInfo dto = chainData.getValue();
                List<Label_Product> labelProducts = labelProductService.getLabelProductsByProductId(
                        chainData.getValue().getProductId()
                );
                List<Label> labels = getLabelByName(chainData.getValue().getLabel());

                if (labelProducts.isEmpty()) {
                    List<String> labelsName = createListLabel(labels, chainData.getValue().getProductId());
                    dto.setLabel(labelsName);
                    chainData.setValue(dto).setSuccess(true);
                } else {
                    for (Label_Product labelProduct : labelProducts) {
                        labelProductService.deleteLabelProduct(labelProduct.getId());
                    }
                    List<String> labelsName = createListLabel(labels, chainData.getValue().getProductId());
                    dto.setLabel(labelsName);
                    chainData.setValue(dto).setSuccess(true);
                }
            } catch (IllegalArgumentException e) {
                // Catch validation error và set chainData thành fail
                chainData.setSuccessStatus(false).setMessage(e.getMessage());
            }
        }
        return new Chain<>(this);
    }

    private List<Label> getLabelByName(List<String> labels) {
        List<Label> res = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (String labelName : labels) {
            Optional<Label> labelOptional = labelService.getLabelByName(labelName);
            if (labelOptional.isPresent()) {
                res.add(labelOptional.get());
            } else {
                notFound.add(labelName);
            }
        }

        // Nếu có labels không tồn tại, throw exception
        if (!notFound.isEmpty()) {
            throw new IllegalArgumentException(
                "Các label sau không tồn tại trong hệ thống: " + String.join(", ", notFound) +
                ". Vui lòng tạo các label này trong admin panel trước khi thêm sản phẩm."
            );
        }

        return res;
    }

    private List<String> createListLabel(List<Label> labels, int productId) {
        for (Label label : labels) {
            labelProductService.createLabelProduct(
                    Label_Product.builder()
                            .label_id(label.getId())
                            .product_id(productId)
                            .build()
            );
        }
        return labels.stream().map(Label::getName).toList();
    }
}