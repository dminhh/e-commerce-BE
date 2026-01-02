package com.prod.services.elk;

import com.prod.models.elk.ESProducts;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IESProductService {
    Page<ESProducts> getSortedProducts(int page, int size);
    ESProducts createProduct(ESProducts product);
    ESProducts updateProduct(String dbId, ESProducts product);
    ESProducts findProductByDbId(String id);
    ESProducts updateScore(String dbId, double scoreChange);
    boolean createListProduct(List<ESProducts> product);

    // --- Hàm mới thêm vào ---
    Page<ESProducts> searchProducts(String key, int page, int size);
}