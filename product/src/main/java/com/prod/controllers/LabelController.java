package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.ILabelFacade;
import com.prod.facades.data.LabelInfo;
import com.prod.models.products.Label;
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
@RequestMapping("/api/label")
public class LabelController extends Controller<Label> {
    @Autowired
    private ILabelFacade labelFacade;

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<Label>> createObject(@RequestBody LabelInfo label, HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        labelFacade.createLabel(label)
                );
            } else {
                return notOwner();
            }
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseObject<Label>> updateObject(@RequestBody LabelInfo label, HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        labelFacade.updateLabel(label)
                );
            } else {
                return notOwner();
            }
        } catch (Exception e) {
            return serverError(e);
        }
    }


    @GetMapping("/find")
    public ResponseEntity<ResponseObject<Page<Label>>> getAllLabelsByName(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int limit,
                                                                          @RequestParam String name,
                                                                          @RequestParam(defaultValue = "id") String sortField,
                                                                          @RequestParam(defaultValue = "incr") String sortDirect,
                                                                          HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(
                    labelFacade.getAllLabelsByName(name, page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }

    @GetMapping("/get")
    public ResponseEntity<ResponseObject<Label>> getObjectById(@RequestParam int id, HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null) return accountNotFound();
            if (accountRedis.getRole().getName().equals("ADMIN")) {
                return ResponseEntity.ok().body(
                        labelFacade.getLabel(id)
                );
            } else
                return notOwner();
        } catch (Exception e) {
            return serverError(e);
        }
    }
    @GetMapping("/all")
    public ResponseEntity<ResponseObject<Page<Label>>> getAllLabels(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int limit,
                                                                    @RequestParam(defaultValue = "id") String sortField,
                                                                    @RequestParam(defaultValue = "incr") String sortDirect) {
        try {
            return ResponseEntity.ok().body(
                    labelFacade.getAllLabels(page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return serverErrorP();
        }
    }

//    @GetMapping("/delete")
//    public ResponseEntity<ResponseObject<Boolean>> deleteObject(@RequestBody LabelDTO label, HttpServletRequest request) {
//        try {
//            return null;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseEntity.badRequest().body(
//                    ResponseObject.<Boolean>builder()
//                            .message(SERVER_ERROR)
//                            .build()
//            );
//        }
//    }

    @Override
    public ResponseEntity<ResponseObject<List<Label>>> notOwners() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject<List<Label>>> serverErrors() {
        return null;
    }

    private ResponseEntity<ResponseObject<Page<Label>>> serverErrorP() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<Page<Label>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }
}
