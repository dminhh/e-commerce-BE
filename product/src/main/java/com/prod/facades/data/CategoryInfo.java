package com.prod.facades.data;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryInfo {
    private int id;
    @NotNull
    private String name;
    @Builder.Default
    private boolean active = true;
}
