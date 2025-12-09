package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.ICategoryFacade;
import com.prod.facades.data.CategoryInfo;
import com.prod.models.details.Category;
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
@RequestMapping("/api/category")
public class CategoryController extends Controller<Category> {
    @Autowired
    private ICategoryFacade categoryFacade;

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<Category>> createObject(@RequestBody CategoryInfo categoryInfo,
                                                                 HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        categoryFacade.createCategory(categoryInfo)
                );
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseObject<Category>> updateObject(@RequestBody CategoryInfo categoryInfo,
                                                                 HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        categoryFacade.updateCategory(categoryInfo)
                );
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseObject<Page<Category>>> getAllCates(@RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "10") int limit,
                                                                      @RequestParam(defaultValue = "id") String sortField,
                                                                      @RequestParam(defaultValue = "incr") String sortDirect) {
        try {
            return ResponseEntity.ok().body(
                    categoryFacade.getAllCategories(page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }

    @GetMapping("/find")
    public ResponseEntity<ResponseObject<Page<Category>>> getAllCatesByName(@RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "10") int limit,
                                                                            @RequestParam(defaultValue = "") String name,
                                                                            @RequestParam(defaultValue = "id") String sortField,
                                                                            @RequestParam(defaultValue = "incr") String sortDirect) {
        try {
            return ResponseEntity.ok().body(
                    categoryFacade.getCategoriesByName(name, page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }

    @Override
    public ResponseEntity<ResponseObject<List<Category>>> notOwners() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject<List<Category>>> serverErrors() {
        return null;
    }

    public ResponseEntity<ResponseObject<Page<Category>>> serverErrorP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<Category>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

//    @PostMapping("/delete")
//    public ResponseEntity<ResponseObject<Boolean>> deleteObject(@RequestBody CategoryDTO data, HttpServletRequest request) {
//        try {
//            AccountDTO accountDTO = getAccount(request);
//            if (accountDTO.getRole().getName().equals("ADMIN")) {
//                return ResponseEntity.ok().body(categoryFacade.deleteCategory(data));
//            } else return ResponseEntity.badRequest().body(
//                    ResponseObject.<Boolean>builder()
//                            .message(NOT_OWNER)
//                            .build()
//            );
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseEntity.badRequest().body(
//                    ResponseObject.<Boolean>builder()
//                            .message(SERVER_ERROR)
//                            .build()
//            );
//        }
//    }

    @GetMapping("/get")
    public ResponseEntity<ResponseObject<Category>> getObjectById(@RequestParam int id, HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(categoryFacade.getCategoryById(id));
            } else return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }

}
