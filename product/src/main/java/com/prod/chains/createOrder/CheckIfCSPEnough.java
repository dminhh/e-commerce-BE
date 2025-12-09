package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.models.ENUM.Order_Status;
import com.prod.models.carts.Small_Quantity;
import com.prod.services.carts.ISmallQuantityService;
import com.prod.services.details.IQuantityService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
public class CheckIfCSPEnough implements ChainHandler<CSPCartInfo> {
    private final ISmallQuantityService smallQuantityService;
    private final IQuantityService quantityService;
    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        if (chainData.isSuccess()){
            CSPCartInfo dto = chainData.getValue();
            Optional<Small_Quantity> smallQuantity = smallQuantityService.getByCSProductId(dto.getCsp_id());
            if (smallQuantity.isPresent()){
                int quantity = smallQuantity.get().getQuantity();
                if (quantity > dto.getQuantity()){
                    Small_Quantity sm = smallQuantityService.update(smallQuantity.get(), dto.getQuantity());
                    quantityService.updateQuantity(dto.getQuantity(), sm.getQuantity_id());
                    chainData.setOrderStatus(Order_Status.CHO_XAC_NHAN.toString());
                    chainData.setSuccess(true);
                } else {
                    chainData
                            .setMessage("Khong du san pham " + dto.getName() + " trong kho")
                            .setSuccess(false);
                }
            } else {
                chainData
                        .setMessage("Khong tim thay so luong san pham " + dto.getName() + " trong kho")
                        .setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
