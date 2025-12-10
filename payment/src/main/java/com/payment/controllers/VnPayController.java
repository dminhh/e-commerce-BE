package com.payment.controllers;

import com.common.DTO.ResponseObject;
import com.payment.entity.Payment;
import com.payment.service.OrderFeign;
import com.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class VnPayController {
    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);
    private final PaymentService paymentService;
    private final OrderFeign orderFeign;
    @GetMapping("/vn-pay")
    public ResponseEntity<ResponseObject<Payment.VNPayResponse>> pay(HttpServletRequest request) {
        Payment.VNPayResponse paymentResponse = paymentService.createVnPayPayment(request);
        ResponseObject<Payment.VNPayResponse> responseObject = ResponseObject.<Payment.VNPayResponse>builder()
                .isSuccess(true)
                .data(paymentResponse)
                .message("Thanh toán thành công!")
                .build();
        return ResponseEntity.ok(responseObject);
//                new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }
    @GetMapping("/vn-pay-callback")
    public  String payCallbackHandler(HttpServletRequest request, Model model) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            try {
                String orderId = request.getParameter("vnp_TxnRef");
                Object rsp= orderFeign.paySusses(Integer.parseInt(orderId));
//                Object rsp2= orderFeign.changeOrder(Integer.parseInt(orderId));
//            return ResponseEntity.badRequest().body( ResponseObject.<PaymentDTO.VNPayResponse>builder()
//                    .isSuccess(true)
//                    .data(null )
////                    PaymentDTO.VNPayResponse("00", "Success", "")
//                    .message("Thanh cong")
//                    .build());
                model.addAttribute("status", "success");
                model.addAttribute("message", "Thanh toán thành công!");
                return "paymentSuccess";
            }catch(Exception e){
                log.error(e.getMessage());
                model.addAttribute("status", "failure");
                model.addAttribute("message", "Thanh toán thất bại. Vui lòng thử lại.");
                return "paymentFailure";  // Chuyển hướng đến giao diện thất bại
            }

//                    ResponseObject<>(HttpStatus.OK, "Success", new PaymentDTO.VNPayResponse("00", "Success", ""));
        } else {
//            return ResponseEntity.badRequest().body(
//                    ResponseObject.<PaymentDTO.VNPayResponse>builder()
//                            .isSuccess(false)
//                            .data(null)
//                            .message("that bại")
//                            .build()
//            );
            model.addAttribute("status", "failure");
            model.addAttribute("message", "Thanh toán thất bại. Vui lòng thử lại.");
            return "paymentFailure";  // Chuyển hướng đến giao diện thất bại
//                    new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", null);
        }
    }

//    private final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//    private final String MERCHANT_CODE = "YOUR_MERCHANT_CODE";
//    private final String API_KEY = "YOUR_API_KEY";
//    private final String VNPAY_RETURN_URL = "http://localhost:3000/payment-result";
//
//    @PostMapping("/createPayment")
//    public String createPayment(@RequestBody Map<String, Object> request) {
//        String orderId = RandomStringUtils.randomNumeric(8); // Mã giao dịch mới
//        String amount = request.get("amount").toString();
//        String orderInfo = "Payment for order " + orderId;
//        String currCode = "VND";
//
//        Date now = new Date();
//        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
//
//        String vnp_TxnRef = orderId; // Mã giao dịch
//        String vnp_Amount = String.valueOf(Double.parseDouble(amount) * 100);
//        String vnp_SecureHash = generateSecureHash(vnp_TxnRef, vnp_Amount, createDate);
//
//        StringBuilder vnpUrl = new StringBuilder(VNPAY_URL);
//        vnpUrl.append("?vnp_Version=2.0.0")
//                .append("&vnp_Command=pay")
//                .append("&vnp_TmnCode=" + MERCHANT_CODE)
//                .append("&vnp_TransactionType=pay")
//                .append("&vnp_OrderInfo=" + orderInfo)
//                .append("&vnp_TxnRef=" + vnp_TxnRef)
//                .append("&vnp_Amount=" + vnp_Amount)
//                .append("&vnp_CurrCode=" + currCode)
//                .append("&vnp_CreateDate=" + createDate)
//                .append("&vnp_ReturnUrl=" + VNPAY_RETURN_URL)
//                .append("&vnp_SecureHash=" + vnp_SecureHash);
//
//        return vnpUrl.toString(); // Trả về URL thanh toán
//    }
//
//    private String generateSecureHash(String txnRef, String amount, String createDate) {
//        String data = "vnp_TmnCode=" + MERCHANT_CODE
//                + "&vnp_Amount=" + amount
//                + "&vnp_CurrCode=VND"
//                + "&vnp_OrderInfo=Payment for order " + txnRef
//                + "&vnp_TxnRef=" + txnRef
//                + "&vnp_CreateDate=" + createDate;
//
//        return Utils.hmacSHA256(data, API_KEY); // Phương thức mã hóa
//    }
}