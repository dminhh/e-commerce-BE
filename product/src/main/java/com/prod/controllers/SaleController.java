package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.ISaleFacade;
import com.prod.facades.data.SaleInfo;
import com.prod.models.products.Sale;
import com.prod.redis.AccountRedis;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sale")
public class SaleController extends Controller<Sale> {
    @Autowired
    private ISaleFacade saleFacade;
    @Override
    public ResponseEntity<ResponseObject<List<Sale>>> notOwners() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<List<Sale>>builder()
                        .message(NOT_OWNER)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject<List<Sale>>> serverErrors() {
        return null;
    }
    public ResponseEntity<ResponseObject<Page<Sale>>> serverErrorP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<Sale>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<Sale>> createObject(@RequestBody SaleInfo data,
                                                             HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().toString().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        saleFacade.createSale(data)
                );
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseObject<Sale>> updateObject(@RequestBody SaleInfo data,
                                                             HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().toString().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        saleFacade.updateSaleProduct(data)
                );
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

//    @GetMapping("/delete")
//    public ResponseEntity<ResponseObject<Sale>> deleteObject(@RequestBody SaleDTO data,
//                                                             HttpServletRequest request) {
//        try {
//            AccountDTO accountDTO = getAccount(request);
//            if (accountDTO == null) return accountNotFound();
//            if (accountDTO.getRole().toString().equals("ADMIN")) {
//                return ResponseEntity.ok().body(
//                        saleFacade.deleteSaleById(data)
//                );
//            } else return notOwner();
//        } catch (Exception e) {
//            return serverError(e);
//        }
//    }

    @GetMapping("/get")
    public ResponseEntity<ResponseObject<Sale>> getObjectById(@RequestParam int id, HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().toString().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        saleFacade.getSaleById(id)
                );
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GetMapping("/bySeasonId")
    public ResponseEntity<ResponseObject<Page<Sale>>> getAllSalesBySeasonId(@RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "10") int limit,
                                                                            @RequestParam(defaultValue = "id") String sortField,
                                                                            @RequestParam(defaultValue = "incr") String sortDirect,
                                                                            @RequestBody SaleInfo saleInfo) {
        try {
                return ResponseEntity.ok().body(
                        saleFacade.getAllSalesBySeasonId(saleInfo, page, limit)
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }

    @GetMapping("/inPass")
    public ResponseEntity<ResponseObject<Page<Sale>>> getAllSalesInPass(@RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int limit,
                                                                        @RequestParam(defaultValue = "id") String sortField,
                                                                        @RequestParam(defaultValue = "incr") String sortDirect) {
        try {
            return ResponseEntity.ok().body(
                    saleFacade.getAllSalesInPass(page, limit)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }
    @GetMapping("/inFuture")
    public ResponseEntity<ResponseObject<Page<Sale>>> getAllSalesInFuture(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int limit,
                                                                          @RequestParam(defaultValue = "id") String sortField,
                                                                          @RequestParam(defaultValue = "incr") String sortDirect){
        try {
            return ResponseEntity.ok().body(
                    saleFacade.getAllSalesInFuture(page, limit)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }
    @GetMapping("/between")
    public ResponseEntity<ResponseObject<Page<Sale>>> getAllSalesBetweenDate(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int limit,
                                                                             @RequestParam(defaultValue = "id") String sortField,
                                                                             @RequestParam(defaultValue = "incr") String sortDirect,
                                                                             @RequestBody SaleInfo saleInfo){
        try {
            return ResponseEntity.ok().body(
                    saleFacade.getAllSalesBetweenDate(saleInfo, page, limit)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }
    @GetMapping("/now")
    public ResponseEntity<ResponseObject<Page<Sale>>> getAllSalesByNow(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int limit,
                                                                       @RequestParam(defaultValue = "id") String sortField,
                                                                       @RequestParam(defaultValue = "incr") String sortDirect){
        try {
            return ResponseEntity.ok().body(
                    saleFacade.getAllSalesByNow(page, limit)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }
}
