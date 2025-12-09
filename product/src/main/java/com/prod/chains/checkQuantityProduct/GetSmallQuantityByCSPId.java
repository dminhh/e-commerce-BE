package com.prod.chains.checkQuantityProduct;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CartInfo;
import com.prod.models.carts.Small_Quantity;
import com.prod.services.caches.IEmailSender;
import com.prod.services.carts.ISmallQuantityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class GetSmallQuantityByCSPId implements ChainHandler<CartInfo> {
    private final ISmallQuantityService smallQuantityService;
    private final IEmailSender emailSender;
    @Override
    public Chain<CartInfo> handle(ChainData<CartInfo> chainData) {
        if(!chainData.isSuccess()) {
            return new Chain<>(this);
        }
        int csp_id = chainData.getValue().getCspId();
        Optional<Small_Quantity> s_quantity = smallQuantityService.getByCSProductId(
                csp_id
        );
        if(s_quantity.isPresent()) {
            if (s_quantity.get().getQuantity() == 0) {
                chainData.setMessage("Het_Hang")
                        .setSuccess(true);
                emailSender.outOfProduct(
                        chainData.getUserEmail(),
                        chainData.getValue().getProduct()
                );
            } else if (s_quantity.get().getQuantity() <= 5) {
                chainData.setMessage("Gan_het_Hang:" + s_quantity.get().getQuantity())
                        .setSuccess(true);
                emailSender.lowQuantityProduct(
                        chainData.getUserEmail(),
                        chainData.getValue().getProduct(),
                        s_quantity.get().getQuantity()
                );
            }
        } else {
            chainData.setMessage("Khong_tim_thay_so_luong_san_pham")
                    .setSuccess(false);
        }
        return new Chain<>(this);
    }
}
