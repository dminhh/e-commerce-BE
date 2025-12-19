package com.prod.RAG.model;

import com.prod.facades.data.ProductInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisualSearchResult {
    private String message;

    @Builder.Default
    private List<ProductInfo> products = null;
}
