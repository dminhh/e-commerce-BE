package com.prod.chains.getOrders;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.OrderInfo;
import com.prod.models.orders.Bill;
import com.prod.services.orders.IBillService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
public class GetUserDetail implements ChainHandler<OrderInfo> {
    private final IBillService billService;
    @Override
    public Chain<OrderInfo> handle(ChainData<OrderInfo> chainData) {
        if (chainData.isSuccess()){
            OrderInfo dto = chainData.getValue();
            Optional<Bill> bill = billService.getBillByOrderId(dto.getId());
            if (bill.isPresent()){
                dto.setUser_name(bill.get().getUser());
                dto.setAddress_name(bill.get().getAddress());
                dto.setPhone(bill.get().getPhone());
                chainData.setValue(dto).setSuccess(true);
            } else {
                chainData.setMessage("Khong tim thay hoa don").setSuccess(false);
            }
        }
        return new Chain<>(this);
    }
}
