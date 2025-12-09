package com.prod.facades;

import com.prod.facades.data.BillInfo;
import com.prod.facades.data.OrderInfo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface IOrderFacade {
    void createOrder(OrderInfo orderInfo);
    BillInfo updateBill(BillInfo billInfo);
    Page<OrderInfo> getOrdersByUserId(int userId, int page, int limit);
    Page<BillInfo> getBillsByUserId(int userId, int page, int limit);
    OrderInfo changeStatus(int id);
    OrderInfo cancelOrder(int id);
    Page<OrderInfo> getAllOrders(int page, int limit);
    Page<OrderInfo> getAllOrdersByStatus(String status, int page, int limit);
    Page<OrderInfo> getAllOrdersByStatusAndUserId(String status, int user_id, int page, int limit);
    BillInfo changeStatusBill(int id);
    Page<BillInfo> getAllBills(String key, int page, int limit);
    Page<BillInfo> getAllBillsByStatus(String key, String status, int page, int limit);
    Page<BillInfo> getAllBillsByStatusAndUserId(String status, int user_id, int page, int limit);
}
