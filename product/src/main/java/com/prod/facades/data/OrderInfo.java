package com.prod.facades.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prod.validators.ProductInOrder;
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
public class OrderInfo {
    @JsonProperty("id")
    private int id;
    //user
    @JsonProperty("user_id")
    private int user_id;
    @JsonProperty("user_name")
    private String user_name;
    @JsonProperty("user_email")
    private String user_email;
    @JsonProperty("address_name")
    private String address_name;
    @JsonProperty("phone")
    private String phone;
    //products
    @JsonProperty("products")
    @ProductInOrder
    private List<OrderProductInfo> products;
    @JsonProperty("total")
    private long total;
    //order
    @JsonProperty("status")
    private String status;
    private LocalDateTime date;
    private boolean have_paid;
}
