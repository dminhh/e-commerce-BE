package com.prod.facades.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewInfo {
    private int id;
    private int product_id;
    private double value;
    private String user_name;
    private int user_id;
    @Nullable
    private String content;
}
