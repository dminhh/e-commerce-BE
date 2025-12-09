package com.prod.services.orders.impl;

import com.prod.JPARepositories.orders.BillRepository;
import com.prod.models.orders.Bill;
import com.prod.services.ServicePage;
import com.prod.services.orders.IBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.prod.JPARepositories.orders.BillRepository.Specs.*;

@Service
public class BillService extends ServicePage<Bill> implements IBillService {
    @Autowired
    private BillRepository billRepository;
    @Override
    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }

    @Override
    public Optional<Bill> getBillById(int id) {
        return billRepository.findById(id);
    }

    @Override
    public List<Bill> getBills(String key, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (!Objects.equals(key, "null")) {
            return billRepository.findAll(byUser(key), pageable).getContent();
        }
        return billRepository.findAll(pageable).getContent();
    }

    @Override
    public Optional<Bill> getBillByOrderId(int orderId) {
        return billRepository.findOne(byOrderId(orderId));
    }

    @Override
    public List<Bill> getBillsByStatusAndKey(String key, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (!Objects.equals(key, "null")) {
            return billRepository.findAll(Specification.where(
                    byUser(key).and(byStatus(status))
            ), pageable).getContent();
        }
        return billRepository.findAll(byStatus(status), pageable).getContent();
    }

    @Override
    public List<Bill> getBillsByStatusAndUserId(String status, List<Integer> orderIds, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return billRepository.findAll(Specification.where(byStatus(status).and(byListOrderId(orderIds))), pageable).getContent();
    }

    @Override
    public List<Bill> getBillsByStartAndEndDate(LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.findAll(Specification.where(beforeDate(endDate).and(afterDate(startDate))));
    }
}
