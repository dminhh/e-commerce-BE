package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.IOverviewFacade;
import com.prod.facades.data.OverviewInfo;
import com.prod.models.products.Overview;
import com.prod.redis.UserRedis;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/overview")
public class OverviewController extends Controller<Overview> {
    @Autowired
    private IOverviewFacade overviewFacade;

    @Override
    public ResponseEntity<ResponseObject<List<Overview>>> notOwners() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<List<Overview>>builder()
                        .message(NOT_OWNER)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject<List<Overview>>> serverErrors() {
        return ResponseEntity.badRequest().body(
                ResponseObject.<List<Overview>>builder()
                        .message(SERVER_ERROR)
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseObject<Overview>> createOverview(@RequestBody OverviewInfo data, HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null) return userNotFound();
            return ResponseEntity.ok().body(
                    overviewFacade.createOverview(data)
            );
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<ResponseObject<Overview>> getOverviewsById(@RequestBody OverviewInfo data, HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(
                    overviewFacade.getOverview(data)
            );
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GetMapping("/byUserId")
    public ResponseEntity<ResponseObject<Page<Overview>>> getAllOverviewsByUserId(@RequestParam(defaultValue = "1") int page,
                                                                                  @RequestParam(defaultValue = "10") int limit,
                                                                                  @RequestParam(defaultValue = "id") String sortField,
                                                                                  @RequestParam(defaultValue = "incr") String sortDirect,
                                                                                  HttpServletRequest request) {
        try {
            UserRedis userRedis = getUser(request);
            if (userRedis == null)
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<Overview>>builder()
                                .message(NOT_FOUND_USER)
                                .build()
                );
            OverviewInfo dto = OverviewInfo.builder()
                    .user_id(userRedis.getId())
                    .build();
            return ResponseEntity.ok().body(
                    overviewFacade.getAllOverviewsByUserId(dto, page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<Page<Overview>>builder()
                            .message(SERVER_ERROR)
                            .build()
            );
        }
    }

    @GetMapping("/byProductId")
    public ResponseEntity<ResponseObject<Page<Overview>>> getAllOverviewsByProductId(@RequestParam(defaultValue = "1") int page,
                                                                                     @RequestParam(defaultValue = "10") int limit,
                                                                                     @RequestParam int id,
                                                                                     @RequestParam(defaultValue = "id") String sortField,
                                                                                     @RequestParam(defaultValue = "incr") String sortDirect,
                                                                                     HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(
                    overviewFacade.getAllOverviewsByProductId(
                            OverviewInfo.builder().product_id(id).build(),
                            page, limit, sortField, sortDirect)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<Page<Overview>>builder()
                            .message(SERVER_ERROR)
                            .build()
            );
        }
    }
}
