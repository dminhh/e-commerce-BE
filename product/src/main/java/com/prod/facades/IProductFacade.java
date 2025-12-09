package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IProductFacade {
    //create
    ResponseObject<ProductInfo> createProduct(ProductInfo product);

    //update
    ResponseObject<ProductInfo> updateProduct(ProductInfo productInfo);

    //read
    ResponseObject<Page<ProductInfo>> getProducts(int page, int size, int category, int season, List<Integer> label, String sortField, String sortDirection);

    ResponseObject<Page<ProductInfo>> getProductsByLabelId(int id, int page, int size, String sortField, String sortDirect);

    ResponseObject<Page<ProductInfo>> getProductsBySeasonId(int id, int page, int size, String sortField, String sortDirect);

    ResponseObject<ProductInfo> getProductById(int id);

    ResponseObject<Page<ProductInfo>> findProduct(String key, int category, int season, List<Integer> labels, int page, int size, String sortField, String sortDirection);

    ResponseObject<Page<ProductInfo>> getProductsByCategoryId(int id, int page, int size, String sortField, String sortDirect);
}
