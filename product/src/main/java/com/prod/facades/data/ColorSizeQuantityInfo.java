package com.prod.facades.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColorSizeQuantityInfo {
    private String color;
    private String code;
    private String size;
    private int color_id;
    private int size_id;
    private int quantity;
    private int csq_id;
    @Builder.Default
    private int sold = 0;
}
