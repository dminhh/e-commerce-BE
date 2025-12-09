package com.prod.chains.createProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.ProductInfo;
import com.prod.models.details.Category;
import com.prod.services.details.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetCategoryById implements ChainHandler<ProductInfo> {
    private final ICategoryService categoryService;

    @Override
    public Chain<ProductInfo> handle(ChainData<ProductInfo> chainData) {
        if (chainData.isSuccess()) {
            Optional<Category> _cate = categoryService.getCategoryById(
                    chainData.getValue().getCategory()
            );
            if (_cate.isEmpty()) {
                chainData.setMessage("Khong tim thay category")
                        .setSuccess(false);
            } else {
                ProductInfo dto = chainData.getValue();
                dto.setCategoryName(_cate.get().getName());
                chainData.setValue(dto);
                chainData.setSuccess(true);
            }
        }
        return new Chain<>(this);
    }
}
