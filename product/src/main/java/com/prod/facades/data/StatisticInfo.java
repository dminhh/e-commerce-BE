package com.prod.facades.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticInfo {
    //input
    private LocalDateTime start;
    private LocalDateTime end;
    //output
    private List<BillInfo> bills;
    private long total;
    private int productInCart;
    private int productOutCart;
    private int billPaid;
    private int billUnPaid;
    private String user;
    private int userId;
}
