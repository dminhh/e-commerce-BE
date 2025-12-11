package com.payment.service;

import com.payment.entity.Payment;
import com.payment.configs.VNPAYConfig;
import com.payment.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPAYConfig vnPayConfig;
    public Payment.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        try {

//            String decryptedAmount = decryptAmount(request.getParameter("amount"), "toilamotchillguysieucapcutedethuongsieucap");
            long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
//            long amount = Integer.parseInt(decryptedAmount) * 100L;
            String bankCode = request.getParameter("bankCode");
            String orderId = request.getParameter("orderId");
            // Kiểm tra nếu mã đơn hàng không có trong request thì trả về lỗi
            if (orderId == null || orderId.isEmpty()) {
                return Payment.VNPayResponse.builder()
                        .code("error")
                        .message("Mã đơn hàng không hợp lệ")
                        .build();
            }
            Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
            vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
            vnpParamsMap.put("vnp_TxnRef", orderId);  // Thêm mã đơn hàng vào tham số vnp_TxnRef
            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", bankCode);
            }
            vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
            //build query url
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
            return Payment.VNPayResponse.builder()
                    .code("ok")
                    .message("success")
                    .paymentUrl(paymentUrl).build();
        } catch (Exception e) {
            return Payment.VNPayResponse.builder()
                    .code("error")
                    .message("Sai mã")
                    .build();
        }


    }
    // Hàm giải mã
    public static String decryptAmount(String encryptedAmount, String secretKey) throws Exception {
        // Chuyển secret key thành dạng byte (16 bytes - AES yêu cầu)
        byte[] keyBytes = secretKey.getBytes("UTF-8");
        SecretKey key = new SecretKeySpec(keyBytes, 0, 16, "AES");

        // Khởi tạo cipher với AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Giải mã chuỗi mã hóa
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedAmount);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        // Chuyển về chuỗi ban đầu
        return new String(decryptedBytes, "UTF-8");
    }

//    public String createQR(Double amount, String orderInfo) throws Exception {
//        Map<String, String> params = new HashMap<>();
//        params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());//TMN_CODE
//        params.put("vnp_Amount", String.valueOf(amount * 100));  // Số tiền cần chuyển sang đơn vị nhỏ nhất
//        params.put("vnp_OrderInfo", orderInfo);
//        params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
//        params.put("vnp_NotifyUrl", vnPayConfig.);
//        params.put("vnp_OrderType", ORDER_TYPE);
//        params.put("vnp_Version", VERSION);
//        params.put("vnp_Command", "pay");
//
//        // Tạo chuỗi ký tự để mã hóa (checksum)
//        String checksum = createChecksum(params);
//        params.put("vnp_SecureHash", checksum);
//
//        // Gửi yêu cầu đến VNPay để lấy URL thanh toán
//        StringBuilder url = new StringBuilder(VN_PAY_URL);
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
//        }
//        url.deleteCharAt(url.length() - 1);  // Loại bỏ dấu & cuối cùng
//
//        return url.toString();  // URL này chứa mã QR
//    }
//
//    private String createChecksum(Map<String, String> params) {
//        // Mã hóa các tham số để tạo ra checksum
//        StringBuilder hashData = new StringBuilder();
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
//        }
//        hashData.deleteCharAt(hashData.length() - 1);  // Loại bỏ dấu & cuối cùng
//        return VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());  // Hàm mã hóa
//    }

}
