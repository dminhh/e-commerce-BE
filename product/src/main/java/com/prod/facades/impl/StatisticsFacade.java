package com.prod.facades.impl;

import com.prod.chains.Chain;
import com.prod.chains.data.ChainData;
import com.prod.chains.getOrders.CheckIfReviewed;
import com.prod.chains.getOrders.GetProductByOrderProduct;
import com.prod.chains.getOrders.GetUserDetail;
import com.prod.facades.IStatisticsFacade;
import com.prod.facades.data.BillInfo;
import com.prod.facades.data.OrderInfo;
import com.prod.facades.data.OrderProductInfo;
import com.prod.facades.data.StatisticInfo;
import com.prod.models.ENUM.Bill_Status;
import com.prod.models.orders.Bill;
import com.prod.models.orders.Order;
import com.prod.models.orders.Order_Product;
import com.prod.services.carts.IColorService;
import com.prod.services.carts.IColorSizeProductService;
import com.prod.services.carts.ISizeService;
import com.prod.services.orders.IBillService;
import com.prod.services.orders.IOrderProductService;
import com.prod.services.orders.IOrderService;
import com.prod.services.products.IImageService;
import com.prod.services.products.IProductService;
import com.prod.services.products.IReviewService;
import com.prod.utils.ConvertListPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsFacade implements IStatisticsFacade {
    @Autowired
    private IProductService productService;
    @Autowired
    private IColorService colorService;
    @Autowired
    private ISizeService sizeService;
    @Autowired
    private IColorSizeProductService cspService;
    @Autowired
    private IImageService imageService;
    @Autowired
    private IBillService billService;
    @Autowired
    private IReviewService reviewService;
    @Autowired
    private IOrderProductService orderProductService;
    @Autowired
    private IOrderService orderService;

    @Override
    public Page<StatisticInfo> getAllStatistics(int page, int size, LocalDateTime start, LocalDateTime end) {
        ConvertListPage<StatisticInfo> convertListPage = new ConvertListPage<>();
        // Lấy danh sách hóa đơn trong khoảng thời gian
        List<Bill> bills = billService.getBillsByStartAndEndDate(start, end);

        // Nhóm hóa đơn theo user_id
        Map<Integer, List<Bill>> groupedByUser = bills.stream()
                .collect(Collectors.groupingBy(b -> {
                    Optional<Order> order = orderService.getOrderById(b.getOrder_id());
                    return order.get().getUser_id();
                })); // user_id của hóa đơn

        // Tính toán thống kê cho từng user
        List<StatisticInfo> statistics = groupedByUser.entrySet().stream()
                .map(entry -> {
                    int userId = entry.getKey();
                    List<Bill> userBills = entry.getValue();

                    // Tính toán chi tiết
                    int productInCart = 0;  // sản phẩm trong giỏ hàng
                    int productOutCart = 0; // sản phẩm ngoài giỏ hàng
                    int billPaid = 0;       // hóa đơn đã thanh toán
                    int billUnPaid = 0;     // hóa đơn chưa thanh toán

                    for (Bill bill : userBills) {
                        // Kiểm tra trạng thái hóa đơn
                        if (bill.getStatus() == Bill_Status.DA_THANH_TOAN) {
                            billPaid++;
                        } else {
                            billUnPaid++;
                        }

                        // Tính số sản phẩm
                        List<OrderProductInfo> orderProducts = setupListOrderProductDTO(bill.getOrder_id());
                        for (OrderProductInfo product : orderProducts) {
                            if (bill.getStatus() == Bill_Status.CHUA_THANH_TOAN) {
                                productInCart += product.getQuantity();
                            } else {
                                productOutCart += product.getQuantity();
                            }
                        }
                    }
                    List<BillInfo> sortedBills = Objects.requireNonNull(getBillDTOsByBills(userBills))
                            .stream()
                            .sorted(Comparator.comparingLong(BillInfo::getTotal).reversed()) // Sắp xếp giảm dần theo tổng
                            .toList();
                    // Tổng số tiền theo người dùng
                    long total = 0;
                    for (BillInfo bill : sortedBills) {
                        total += bill.getTotal();
                    }
                    // Tạo DTO
                    return StatisticInfo.builder()
                            .start(start)
                            .end(end)
                            .userId(userId)
                            .user(userBills.get(0).getUser()) // Tên user (giả định tất cả các hóa đơn của user có cùng tên)
                            .total(total)
                            .productInCart(productInCart)
                            .productOutCart(productOutCart)
                            .billPaid(billPaid)
                            .billUnPaid(billUnPaid)
                            .bills(sortedBills)
                            .build();
                })
                .toList();

        // Chuyển đổi sang Page
        return convertListPage.listToPage(statistics, page, size);
    }

    private List<BillInfo> getBillDTOsByBills(List<Bill> bills) {
        List<BillInfo> res = new ArrayList<>();
        for (Bill bill : bills) {
            res.add(getBillDTO(orderService.getOrderById(bill.getOrder_id()).get(), bill));
        }
        return res;
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
}
