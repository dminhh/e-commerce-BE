package com.prod.facades.impl;

import com.prod.chains.Chain;
import com.prod.chains.createOrder.*;
import com.prod.chains.data.ChainData;
import com.prod.chains.getOrders.CheckIfReviewed;
import com.prod.chains.getOrders.GetProductByOrderProduct;
import com.prod.chains.getOrders.GetUserDetail;
import com.prod.facades.IOrderFacade;
import com.prod.facades.data.*;
import com.prod.models.ENUM.Bill_Status;
import com.prod.models.ENUM.Order_Status;
import com.prod.models.carts.Color_Size_Product;
import com.prod.models.carts.Small_Quantity;
import com.prod.models.details.Quantity;
import com.prod.models.orders.Bill;
import com.prod.models.orders.Order;
import com.prod.models.orders.Order_Product;
import com.prod.services.carts.*;
import com.prod.services.details.IQuantityService;
import com.prod.services.elk.impl.ESProductService;
import com.prod.services.orders.IBillService;
import com.prod.services.orders.IOrderProductService;
import com.prod.services.orders.IOrderService;
import com.prod.services.products.IImageService;
import com.prod.services.products.IProductService;
import com.prod.services.products.IReviewService;
import com.prod.utils.ConvertESToProductDTO;
import com.prod.utils.ConvertListPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j

public class OrderFacade implements IOrderFacade {
    private static final String ECOMMERCE_EMAIL = "montoan01102002@gmail.com";
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IReviewService reviewService;
    @Autowired
    private IOrderProductService orderProductService;
    @Autowired
    private ISmallQuantityService smallQuantityService;
    @Autowired
    private ICartProductService cartProductService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IColorSizeProductService cspService;
    @Autowired
    private IColorService colorService;
    @Autowired
    private ISizeService sizeService;
    @Autowired
    private IBillService billService;
    @Autowired
    private IImageService imageService;
    @Autowired
    private ICartService cartService;
    @Autowired
    private IQuantityService quantityService;
    @Autowired
    private ESProductService esProductService;
    @Autowired
    private ConvertESToProductDTO es;

    @Override
    public void createOrder(OrderInfo orderInfo) {
        boolean available = false;
        Order order = orderService.createOrder(
                Order.builder()
                        .user_id(orderInfo.getUser_id())
                        .create_at(LocalDateTime.now())
                        .update_at(LocalDateTime.now())
                        .date(LocalDateTime.now())
                        .build()
        );
        //create order product
        for (OrderProductInfo dto : orderInfo.getProducts()) {
            ChainData<CSPCartInfo> data = ChainData.<CSPCartInfo>builder()
                    .value(CSPCartInfo.builder()
                            .quantity(dto.getQuantity())
                            .csp_id(dto.getCsp_id())
                            .order_id(order.getId())
                            .build())
                    .phone(orderInfo.getPhone())
                    .address(orderInfo.getAddress_name())
                    .userName(orderInfo.getUser_name())
                    .userEmail(orderInfo.getUser_email())
                    .userId(orderInfo.getUser_id())
                    .build();
            Chain<CSPCartInfo> chain = new Chain<CSPCartInfo>()
                    .add(new GetCartByUserId(cartService))
                    .add(new GetCSPById(cspService))
                    .add(GetColorSizeByCSP.builder()
                            .colorService(colorService)
                            .sizeService(sizeService)
                            .cspService(cspService)
                            .build())
                    .add(GetProductById.builder()
                            .imageService(imageService)
                            .productService(productService)
                            .build())
                    .add(CheckIfCSPEnough.builder()
                            .quantityService(quantityService)
                            .smallQuantityService(smallQuantityService)
                            .build())
                    .add(CreateOrderProductIfTrue.builder()
                            .orderProductService(orderProductService)
                            .cartProductService(cartProductService)
                            .build())
                    .add(new CallNotification());
            chain.execute(data);
            if (!data.isSuccess()) {
                log.error(data.getMessage());
            } else {
                log.info("Dat hang thanh cong");
                available = true;
//                long total = order.getTotal();
//                order.setTotal(total + data.getValue().getQuantity() * data.getValue().getPrice());
            }
        }
        if (available) {
            order.setUpdate_at(LocalDateTime.now());
            orderService.createOrder(order);
            billService.createBill(
                    Bill.builder()
                            .address(orderInfo.getAddress_name())
                            .phone(orderInfo.getPhone())
                            .user(orderInfo.getUser_name())
                            .order_id(order.getId())
                            .build()
            );
        } else {
            orderService.deleteOrderById(order.getId());
        }
    }
    //muon hot test thi doi rate nay la se thay thay doi lien
    //gia tri dang nhap o day la 1 tieng chay lai ham 1 lan
    @Scheduled(fixedRate = 3600000)
    private void cleanUpExpiredOrders() {
        //muon test khoang thoi gian thi vao ham nay
        List<Order> orders = orderService.getOrderExpired();
        if (!orders.isEmpty()){
            for (Order order : orders){
                orderService.cancelOrder(order);
                List<Order_Product> orderProducts = orderProductService.getOrderProductsByOrderId(order.getId());
                if (!orderProducts.isEmpty()){
                    for (Order_Product order_product : orderProducts){
                        Optional<Small_Quantity> smallQuantity = smallQuantityService.getByCSProductId(order_product.getId());
                        cancelOrderProduct(smallQuantity, order_product.getQuantity());
                        changeScore(order_product.getCsp_id());
                    }
                }
            }
        }
    }
    private void changeScore(int cspId) {
        Optional<Color_Size_Product> csp = cspService.getColorSizeProductById(cspId);
        try {
            if (csp.isPresent()){
                esProductService.updateScore(csp.get().getProduct_id() + "", -15);
            }
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }
    @Override
    public BillInfo updateBill(BillInfo billInfo) {
        Optional<Bill> bill = billService.getBillById(billInfo.getId());
        if (bill.isPresent()) {
            bill.get().setUpdate_at(LocalDateTime.now());
            bill.get().setPhone(billInfo.getPhone());
            bill.get().setAddress(billInfo.getAddress());
            bill.get().setUser(billInfo.getUser());
            Bill nBill = billService.createBill(bill.get());
            Optional<Order> order = orderService.getOrderById(bill.get().getOrder_id());
            if (order.isPresent()) {
                return getBillDTO(order.get(), nBill);
            }
            return null;
        }
        return null;
    }

    @Override
    public Page<OrderInfo> getOrdersByUserId(int userId, int page, int limit) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<OrderInfo> res = new ArrayList<>();
        for (Order order : orders) {
            Optional<Bill> bill = billService.getBillByOrderId(order.getId());
            if (bill.isPresent()) {
                ChainData<OrderInfo> chainData = getOrderDTO(
                        orderService.getOrderById(bill.get().getOrder_id()).get()
                );
                if (chainData.isSuccess()) {
                    res.add(chainData.getValue());
                } else {
                    log.error(chainData.getMessage());
                }
            }
        }
        ConvertListPage<OrderInfo> convertListPage = new ConvertListPage<>();
        return convertListPage.listToPage(res, page, limit);
    }

    @Override
    public Page<BillInfo> getBillsByUserId(int userId, int page, int limit) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<BillInfo> res = new ArrayList<>();
        for (Order order : orders) {
            Optional<Bill> bill = billService.getBillByOrderId(order.getId());
            if (bill.isPresent()) {
                res.add(getBillDTO(order, bill.get()));
            }
        }
        ConvertListPage<BillInfo> convertListPage = new ConvertListPage<>();
        return convertListPage.listToPage(res, page, limit);
    }

    private BillInfo getBillDTO(Order order, Bill bill) {
        ChainData<OrderInfo> orderDTO = getOrderDTO(order);
        return BillInfo.builder()
                .order_id(order.getId())
                .user(bill.getUser())
                .phone(bill.getPhone())
                .address(bill.getAddress())
                .id(bill.getId())
                .bill_status(bill.getStatus().toString())
                .products(orderDTO.getValue().getProducts())
                .total(orderDTO.getValue().getTotal())
                .order_status(orderDTO.getValue().getStatus())
                .dateTime(orderDTO.getValue().getDate())
                .build();
    }

    @Override
    public OrderInfo changeStatus(int id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            ChainData<OrderInfo> dto = getOrderDTO(orderService.updateStatus(order.get()));
            if (!dto.isSuccess()) {
                log.error(dto.getMessage());
                return null;
            } else {
                try{
                    if (dto.getValue().getStatus().equals(Order_Status.GIAO_HANG_THANH_CONG.toString())) {
                        for (OrderProductInfo d : dto.getValue().getProducts()){
                            esProductService.updateScore(d.getProduct_id() + "", 10);
                        }
                    }
                } catch (Exception e){
                    log.error(e.getMessage());
                }
                return dto.getValue();
            }
        } else {
            log.error("Khong tim thay don hang");
            return null;
        }
    }

    @Override
    public OrderInfo cancelOrder(int id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            ChainData<OrderInfo> dto = getOrderDTO(orderService.cancelOrder(order.get()));
            if (!dto.isSuccess()) {
                log.error(dto.getMessage());
                return null;
            } else {
                for (OrderProductInfo dto1 : dto.getValue().getProducts()) {
                    Optional<Small_Quantity> smallQuantity = smallQuantityService.getByCSProductId(dto1.getCsp_id());
                    cancelOrderProduct(smallQuantity, dto1.getQuantity());
                    changeScore(dto1.getCsp_id());
                }
                return dto.getValue();
            }
        } else {
            log.error("Khong tim thay don hang");
            return null;
        }
    }

    private void cancelOrderProduct(Optional<Small_Quantity> smallQuantity, int quantity2) {
        if (smallQuantity.isPresent()) {
            smallQuantity.get().setQuantity(smallQuantity.get().getQuantity() + quantity2);
            smallQuantity.get().setSold(smallQuantity.get().getSold() - quantity2);
            smallQuantity.get().setUpdate_at(LocalDateTime.now());
            smallQuantityService.create(smallQuantity.get());
            Optional<Quantity> quantity = quantityService.getQuantityById(smallQuantity.get().getQuantity_id());
            if (quantity.isPresent()) {
                quantity.get().setQuantity(quantity.get().getQuantity() + quantity2);
                quantity.get().setSold(quantity.get().getSold() - quantity2);
                quantity.get().setUpdate_at(LocalDateTime.now());
                quantityService.createQuantity(quantity.get());
            }
        }
    }

    @Override
    public Page<OrderInfo> getAllOrders(int page, int limit) {
        List<Order> orders = orderService.getPageOrders(page, limit).getContent();
        return getAllOrderDTO(orders, page, limit);
    }

    private Page<OrderInfo> getAllOrderDTO(List<Order> orders, int page, int limit) {
        ConvertListPage<OrderInfo> convertListPage = new ConvertListPage<>();
        List<OrderInfo> res = new ArrayList<>();
        for (Order order : orders) {
            //setup user
            //setup order
            ChainData<OrderInfo> dto = getOrderDTO(order);
            if (!dto.isSuccess()) {
                log.error(dto.getMessage());
            } else {
                List<OrderProductInfo> products = dto.getValue().getProducts();
                dto.getValue().setTotal(getTotal(products));
                res.add(dto.getValue());
            }
        }
        return convertListPage.listToPage(res, page, limit);
    }

    private ChainData<OrderInfo> getOrderDTO(Order order) {
        List<OrderProductInfo> orderProductInfos = setupListOrderProductDTO(order.getId());
        ChainData<OrderInfo> dto = ChainData.<OrderInfo>builder()
                .value(OrderInfo.builder()
                        //order
                        .id(order.getId())
                        .status(order.getStatus())
                        .date(order.getDate())
                        //user
                        .user_id(order.getUser_id())
                        //products
                        .products(orderProductInfos)
                        .build())
                //them email de thong bao
                .userEmail(ECOMMERCE_EMAIL)
                .build();
        Chain<OrderInfo> chain = new Chain<OrderInfo>()
                .add(GetProductByOrderProduct.builder()
                        //get order product
                        //get product
                        .productService(productService)
                        .colorService(colorService)
                        .sizeService(sizeService)
                        .cspService(cspService)
                        .imageService(imageService)
                        .build())
                .add(GetUserDetail.builder()
                        .billService(billService)
                        .build())
                .add(CheckIfReviewed.builder()
                        .reviewService(reviewService)
                        .billService(billService)
                        .build());
        chain.execute(dto);
        long total = 0;
        if (dto.isSuccess()) {
            for (OrderProductInfo orderProductInfo : orderProductInfos) {
                total += orderProductInfo.getQuantity() * orderProductInfo.getPrice();
            }
            dto.getValue().setTotal(total);
        }
        return dto;
    }

    @Override
    public Page<OrderInfo> getAllOrdersByStatus(String status, int page, int limit) {
        List<String> strings = Arrays.asList(
                Order_Status.CHO_VAN_CHUYEN.toString(),
                Order_Status.CHO_XAC_NHAN.toString(),
                Order_Status.DANG_GIAO.toString(),
                Order_Status.HANG_HOAN.toString(),
                Order_Status.GIAO_HANG_THANH_CONG.toString(),
                Order_Status.HUY_HANG.toString()
        );
        status = status.toUpperCase();
        if (strings.contains(status)) {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return getAllOrderDTO(orders, page, limit);
        }
        return null;
    }

    @Override
    public Page<OrderInfo> getAllOrdersByStatusAndUserId(String status, int user_id, int page, int limit) {
        List<String> strings = Arrays.asList(
                Order_Status.CHO_VAN_CHUYEN.toString(),
                Order_Status.CHO_XAC_NHAN.toString(),
                Order_Status.DANG_GIAO.toString(),
                Order_Status.HANG_HOAN.toString(),
                Order_Status.GIAO_HANG_THANH_CONG.toString(),
                Order_Status.HUY_HANG.toString()
        );
        status = status.toUpperCase();
        if (strings.contains(status)) {
            List<Order> orders = orderService.getOrderByStatusAndUserId(status, user_id);
            return getAllOrderDTO(orders, page, limit);
        }
        return null;
    }

    @Override
    public BillInfo changeStatusBill(int id) {
        Optional<Bill> bill = billService.getBillById(id);
        if (bill.isPresent()) {
            bill.get().setUpdate_at(LocalDateTime.now());
            bill.get().setStatus(Bill_Status.DA_THANH_TOAN);
            Bill nBill = billService.createBill(bill.get());
            Optional<Order> order = orderService.getOrderById(bill.get().getOrder_id());
            if (order.isPresent()) {
                if (order.get().getStatus().equals(Order_Status.CHO_XAC_NHAN.toString())) {
                    order.get().setStatus(Order_Status.CHO_VAN_CHUYEN.toString());
                    order.get().setUpdate_at(LocalDateTime.now());
                    return getBillDTO(orderService.createOrder(order.get()), nBill);
                } else
                    return getBillDTO(order.get(), nBill);
            }
            return null;
        }
        return null;
    }

    @Override
    public Page<BillInfo> getAllBills(String key, int page, int limit) {
        List<Bill> bills = billService.getBills(key, page, limit);
        return getBillDTOsByBills(bills, page, limit);
    }

    @Override
    public Page<BillInfo> getAllBillsByStatus(String key, String status, int page, int limit) {
        status = status.toUpperCase();
        List<Bill> bills = billService.getBillsByStatusAndKey(key, status, page, limit);
        return getBillDTOsByBills(bills, page, limit);
    }

    @Override
    public Page<BillInfo> getAllBillsByStatusAndUserId(String status, int user_id, int page, int limit) {
        status = status.toUpperCase();
        List<Integer> orderIds = orderService.getOrdersByUserId(user_id).stream().map(Order::getId).toList();
        List<Bill> bills = billService.getBillsByStatusAndUserId(status, orderIds, page, limit);
        return getBillDTOsByBills(bills, page, limit);
    }

    private Page<BillInfo> getBillDTOsByBills(List<Bill> bills, int page, int limit) {
        ConvertListPage<BillInfo> convertListPage = new ConvertListPage<>();
        List<BillInfo> res = new ArrayList<>();
        for (Bill bill : bills) {
            res.add(getBillDTO(orderService.getOrderById(bill.getOrder_id()).get(), bill));
        }
        if (res == null)
            return null;
        else
            return convertListPage.listToPage(res, page, limit);
    }

    private List<OrderProductInfo> setupListOrderProductDTO(int id) {
        List<Order_Product> orderProducts = orderProductService.getOrderProductsByOrderId(id);
        List<OrderProductInfo> orderProductInfos = new ArrayList<>();
        orderProducts.forEach(orderProduct -> {
            orderProductInfos.add(
                    OrderProductInfo.builder()
                            .csp_id(orderProduct.getCsp_id())
                            .quantity(orderProduct.getQuantity())
                            .build()
            );
        });
        return orderProductInfos;
    }

    private long getTotal(List<OrderProductInfo> orderProducts) {
        long res = 0;
        for (OrderProductInfo op : orderProducts) {
            res += op.getPrice() * op.getQuantity();
        }
        return res;
    }
}
