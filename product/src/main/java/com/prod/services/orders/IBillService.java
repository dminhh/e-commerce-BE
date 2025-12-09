package com.prod.services.orders;

import com.prod.models.orders.Bill;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface IBillService {
    Bill createBill(Bill bill);

    Optional<Bill> getBillById(int id);

    List<Bill> getBills(String key, int page, int size);

    Optional<Bill> getBillByOrderId(int orderId);

    List<Bill> getBillsByStatusAndKey(String key, String status, int page, int size);

    List<Bill> getBillsByStatusAndUserId(String status, List<Integer> orderIds, int page, int size);
    List<Bill> getBillsByStartAndEndDate(LocalDateTime startDate, LocalDateTime endDate);
}
