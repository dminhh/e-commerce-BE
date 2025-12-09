package com.prod.chains.getOrders;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.OrderInfo;
import com.prod.facades.data.OrderProductInfo;
import com.prod.models.ENUM.Bill_Status;
import com.prod.models.orders.Bill;
import com.prod.models.products.Review;
import com.prod.services.orders.IBillService;
import com.prod.services.products.IReviewService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Builder
public class CheckIfReviewed implements ChainHandler<OrderInfo> {
    private final IReviewService reviewService;
    private final IBillService billService;

    @Override
    public Chain<OrderInfo> handle(ChainData<OrderInfo> chainData) {
        if (chainData.isSuccess()) {
            OrderInfo dto = chainData.getValue();
            Set<Integer> productIds = dto.getProducts().stream().map(OrderProductInfo::getProduct_id).collect(Collectors.toSet());
            for (Integer productId : productIds) {
                Optional<Review> review = reviewService.getReviewByProductIdAndUserId(productId, dto.getUser_id());
                if (review.isPresent()) {
                    //da tung review nen xuat hien review
                    dto.setProducts(changeStatus(dto.getProducts(), false, productId));
                } else
                    //chua tung review nen khong xuat hien review
                    dto.setProducts(changeStatus(dto.getProducts(), true, productId));
            }
            Optional<Bill> bill = billService.getBillByOrderId(dto.getId());
            if (bill.isPresent()) {
                if (bill.get().getStatus().equals(Bill_Status.DA_THANH_TOAN))
                    dto.setHave_paid(true);
            } else dto.setHave_paid(false);
            chainData.setValue(dto).setSuccess(true);
        }
        return new Chain<>(this);
    }

    private List<OrderProductInfo> changeStatus(List<OrderProductInfo> dtos, boolean status, int id) {
        for (OrderProductInfo dto : dtos) {
            if (dto.getProduct_id() == id) {
                dto.setFirst_review(status);
            }
        }
        return dtos;
    }
}
