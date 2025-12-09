package com.prod.validators.vldt;

import com.prod.facades.data.OrderProductInfo;
import com.prod.validators.ProductInOrder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ProductInOrderVldt implements ConstraintValidator<ProductInOrder, List<OrderProductInfo>> {

    @Override
    public boolean isValid(List<OrderProductInfo> dtos,
                           ConstraintValidatorContext constraintValidatorContext) {
        return !dtos.isEmpty();
    }
}