package com.prod.chains.createOrder;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.CSPCartInfo;
import com.prod.facades.data.SendMessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CallNotification implements ChainHandler<CSPCartInfo> {
    private static final String URL = "http://localhost:8888/api/notifications";

    @Override
    public Chain<CSPCartInfo> handle(ChainData<CSPCartInfo> chainData) {
        RestTemplate restTemplate = new RestTemplate();
        if (chainData.isSuccess()) {
            CSPCartInfo dto = chainData.getValue();
            SendMessageInfo sendMessage = new SendMessageInfo("Đặt sản phẩm " +
                    dto.getName() + " thành công !");
            // Tạo đối tượng HttpEntity để gửi yêu cầu
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SendMessageInfo> entity = new HttpEntity<>(sendMessage, headers);

            // Lấy userId từ CSPCartInfo hoặc từ nguồn khác
            String userId = String.valueOf(chainData.getUserId());
            try {
                // Gọi API tạo thông báo
                ResponseEntity<Object> response = restTemplate.exchange(
                        URL + "/" + userId,
                        HttpMethod.POST,
                        entity,
                        Object.class
                );
                // Xử lý phản hồi nếu cần
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Gui thong bao toi nguoi dung thanh cong");
                } else {
                    log.info("Gui thong bao toi nguoi dung that bai");
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return new Chain<>(this);
    }
}
