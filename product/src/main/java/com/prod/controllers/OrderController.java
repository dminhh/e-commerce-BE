package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.IOrderFacade;
import com.prod.facades.data.BillInfo;
import com.prod.facades.data.OrderInfo;
import com.prod.kafka.producers.OrderProducer;
import com.prod.redis.AccountRedis;
import com.prod.redis.UserRedis;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController extends Controller<OrderInfo> {
    @Autowired
    private OrderProducer orderProducer;
    @Autowired
    private IOrderFacade orderFacade;

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<String>> createOrder(@RequestBody OrderInfo orderInfo,
                                                              HttpServletRequest request) {
        try {
            UserRedis user = getUser(request);
            AccountRedis accountRedis = getAccount(request);
            if (user == null)
                return notFoundUser();
            orderInfo.setUser_id(user.getId());
            orderInfo.setUser_email(accountRedis.getEmail());
            orderProducer.send("create-order", orderInfo);
            return ResponseEntity.ok().body(
                    ResponseObject.<String>builder()
                            .message("Dang xu ly don hang")
                            .isSuccess(true)
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @PostMapping("/updateBill")
    public ResponseEntity<ResponseObject<BillInfo>> updateBill(@RequestBody BillInfo billInfo, HttpServletRequest request) {
        try {
            //chi cho phep thay doi phone, user, address
            UserRedis user = getUser(request);
            if (user == null) return notFoundUser();
            return ResponseEntity.ok().body(
                    ResponseObject.<BillInfo>builder()
                            .data(orderFacade.updateBill(billInfo))
                            .isSuccess(true)
                            .message("Da thuc hien cap nhat dia chi don hang")
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/change-status")
    public ResponseEntity<ResponseObject<OrderInfo>> changeStatus(@RequestParam int id,
                                                                  HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                OrderInfo dto = orderFacade.changeStatus(id);
                if (dto == null)
                    return ResponseEntity.badRequest().body(
                            ResponseObject.<OrderInfo>builder()
                                    .message("Khong the thay doi trang thai don hang")
                                    .build()
                    );
                else
                    return ResponseEntity.ok().body(
                            ResponseObject.<OrderInfo>builder()
                                    .data(dto)
                                    .isSuccess(true)
                                    .message("Da cap nhat trang thai don hang")
                                    .build()
                    );
            } else return notOwner();
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverError(e);
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<ResponseObject<OrderInfo>> cancelOrder(@RequestParam int id,
                                                                 HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            OrderInfo dto = orderFacade.cancelOrder(id);
            if (dto == null)
                return ResponseEntity.badRequest().body(
                        ResponseObject.<OrderInfo>builder()
                                .message("Khong the huy don hang")
                                .build()
                );
            else
                return ResponseEntity.ok().body(
                        ResponseObject.<OrderInfo>builder()
                                .data(dto)
                                .isSuccess(true)
                                .message("Da huy don hang thanh cong")
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverError(e);
        }
    }

    @GetMapping("/paid")
    public ResponseEntity<ResponseObject<BillInfo>> paid(@RequestParam int id, HttpServletRequest request) {
        try {
//            UserDTO user = getUser(request);
//            if (user == null) return notFoundUser();
            return ResponseEntity.ok().body(
                    ResponseObject.<BillInfo>builder()
                            .message("Da thanh toan hoa don")
                            .isSuccess(true)
                            .data(orderFacade.changeStatusBill(id))
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/byUserId")
    public ResponseEntity<ResponseObject<Page<OrderInfo>>> findByUserId(@RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int limit,
                                                                        HttpServletRequest request) {
        try {
            UserRedis user = getUser(request);
            if (user == null) return notFoundUser();
//            AccountDTO accountDTO = getAccount(request);
//            if (accountDTO == null) return accountNotFoundP();
//            if (!accountDTO.getRole().getName().equals("ADMIN")) return notOwnersP();
            Page<OrderInfo> res = orderFacade.getOrdersByUserId(user.getId(), page, limit);
            if (res.isEmpty())
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .message("Khong tim thay don dat hang nao cua nguoi dung")
                                .build()
                );
            else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac don dat hang cua nguoi dung")
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/byStatus")
    public ResponseEntity<ResponseObject<Page<OrderInfo>>> getOrdersByStatus(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int limit,
                                                                             @RequestParam(defaultValue = "CHO_XAC_NHAN") String status,
                                                                             HttpServletRequest request) {
        try {
            UserRedis user = getUser(request);
            if (user == null) return notFoundUser();
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFoundP();
            if (!accountRedis.getRole().getName().equals("ADMIN")) return notOwnersP();
            Page<OrderInfo> res = orderFacade.getAllOrdersByStatus(status, page, limit);
            if (res.isEmpty())
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .message("Khong tim thay don dat hang nao co trang thai " + status)
                                .build()
                );
            else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac don dat hang theo trang thai " + status)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/user/byStatus")
    public ResponseEntity<ResponseObject<Page<OrderInfo>>> getOrdersByUserAndStatus(@RequestParam(defaultValue = "1") int page,
                                                                                    @RequestParam(defaultValue = "10") int limit,
                                                                                    @RequestParam(defaultValue = "CHO_XAC_NHAN") String status,
                                                                                    HttpServletRequest request) {
        try {
            UserRedis user = getUser(request);
            if (user == null) return notFoundUser();
            Page<OrderInfo> res = orderFacade.getAllOrdersByStatusAndUserId(status, user.getId(), page, limit);
            if (res.isEmpty())
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .message("Khong tim thay don dat hang nao co trang thai " + status)
                                .build()
                );
            else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac don dat hang theo trang thai " + status)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    private ResponseEntity<ResponseObject<Page<OrderInfo>>> notOwnersP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<OrderInfo>>builder()
                        .message(NOT_OWNER)
                        .build()
        );
    }

    private ResponseEntity<ResponseObject<Page<OrderInfo>>> accountNotFoundP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<OrderInfo>>builder()
                        .message(NOT_FOUND_ACCOUNT)
                        .build()
        );
    }

    @GetMapping("/getBills")
    public ResponseEntity<ResponseObject<Page<BillInfo>>> getBills(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int limit,
                                                                   HttpServletRequest request) {
        try {
            UserRedis user = getUser(request);
            if (user == null) return notFoundUser();
            Page<BillInfo> res = orderFacade.getBillsByUserId(user.getId(), page, limit);
            if (res.isEmpty())
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .message("Khong tim thay hoa don nao cua nguoi dung")
                                .build()
                );
            else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .message("Da tim thay cac hoa don cua nguoi dung")
                                .isSuccess(true)
                                .data(res)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseObject<Page<OrderInfo>>> getAllOrders(@RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFoundP();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<OrderInfo>>builder()
                                .data(orderFacade.getAllOrders(page, size))
                                .isSuccess(true)
                                .message("Da tim thay cac don dat hang")
                                .build()
                );
            } else return notOwnersP();
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/allBills")
    public ResponseEntity<ResponseObject<Page<BillInfo>>> getAllBillsWithKey(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int limit,
                                                                             @RequestParam(defaultValue = "null") String key,
                                                                             HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return notFoundAccount();
            if (!accountRedis.getRole().getName().equals("ADMIN")) return notOwnerWithBills();
            Page<BillInfo> res = orderFacade.getAllBills(key, page, limit);
            if (res.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .message("Khong tim thay danh sach hoa don")
                                .build()
                );
            } else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac hoa don")
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/user/bill/status")
    public ResponseEntity<ResponseObject<Page<BillInfo>>> getAllBillsWithStatusAndUser(@RequestParam(defaultValue = "1") int page,
                                                                                       @RequestParam(defaultValue = "10") int limit,
                                                                                       @RequestParam(defaultValue = "CHUA_THANH_TOAN") String status,
                                                                                       HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null) return notFoundUser();
            Page<BillInfo> res = orderFacade.getAllBillsByStatusAndUserId(status, userRedis.getId(), page, limit);
            if (res.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .message("Khong tim thay danh sach hoa don cua nguoi dung " + status)
                                .build()
                );
            } else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac hoa don cua nguoi dung voi trang thai " + status)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }

    @GetMapping("/allBills/status")
    public ResponseEntity<ResponseObject<Page<BillInfo>>> getAllBillsWithStatusAndKey(@RequestParam(defaultValue = "1") int page,
                                                                                      @RequestParam(defaultValue = "10") int limit,
                                                                                      @RequestParam(defaultValue = "CHUA_THANH_TOAN") String status,
                                                                                      @RequestParam(defaultValue = "null") String key,
                                                                                      HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return notFoundAccount();
            if (!accountRedis.getRole().getName().equals("ADMIN")) return notOwnerWithBills();
            Page<BillInfo> res = orderFacade.getAllBillsByStatus(key,status, page, limit);
            if (res.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .message("Khong tim thay danh sach hoa don voi trang thai " + status)
                                .build()
                );
            } else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<BillInfo>>builder()
                                .data(res)
                                .isSuccess(true)
                                .message("Da tim thay cac hoa don voi trang thai " + status)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverE();
        }
    }


    @Override
    public ResponseEntity<ResponseObject<List<OrderInfo>>> notOwners() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject<List<OrderInfo>>> serverErrors() {
        return null;
    }

    private <T> ResponseEntity<ResponseObject<T>> notFoundUser() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<T>builder()
                        .message(NOT_FOUND_USER)
                        .build());
    }

    private <T> ResponseEntity<ResponseObject<T>> notFoundAccount() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<T>builder()
                        .message(NOT_FOUND_ACCOUNT)
                        .build());
    }

    private <T> ResponseEntity<ResponseObject<T>> serverE() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<T>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

    private <T> ResponseEntity<ResponseObject<Page<T>>> notOwnerWithBills() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<T>>builder()
                        .message(NOT_OWNER)
                        .build()
        );
    }
}
