package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.SaleInfo;
import com.prod.models.products.Sale;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface ISaleFacade {
    ResponseObject<Sale> createSale(SaleInfo saleInfo);
    ResponseObject<Sale> updateSaleProduct(SaleInfo saleInfo);
    ResponseObject<Sale> getSaleById(int id);
    ResponseObject<Page<Sale>> getAllSalesBySeasonId(SaleInfo saleInfo, int page, int size);
    ResponseObject<Page<Sale>> getAllSalesInPass(int page, int size);
    ResponseObject<Page<Sale>> getAllSalesInFuture(int page, int size);
    ResponseObject<Page<Sale>> getAllSalesBetweenDate(SaleInfo saleInfo, int page, int size);
    ResponseObject<Page<Sale>> getAllSalesByNow(int page, int size);
    ResponseObject<Sale> deleteSaleById(SaleInfo saleInfo);
}
