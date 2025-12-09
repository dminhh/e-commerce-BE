package com.prod.facades.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillInfo {
    private int id;
    private String phone;
    private String address;
    private String user;
    private String bill_status;
    private int order_id;
    private List<OrderProductInfo> products;
    private long total;
    private String order_status;
    private LocalDateTime dateTime;
}
