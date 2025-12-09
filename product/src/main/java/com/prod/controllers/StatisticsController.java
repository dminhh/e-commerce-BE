package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.facades.IStatisticsFacade;
import com.prod.facades.data.StatisticInfo;
import com.prod.redis.AccountRedis;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/statistic")
public class StatisticsController extends Controller<StatisticInfo> {
    @Autowired
    private IStatisticsFacade statisticsFacade;

    @Override
    public ResponseEntity<ResponseObject<List<StatisticInfo>>> notOwners() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject<List<StatisticInfo>>> serverErrors() {
        return null;
    }

    @GetMapping("/inRange")
    public ResponseEntity<ResponseObject<Page<StatisticInfo>>> inRange(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "null") String start,
                                                                       @RequestParam(defaultValue = "null") String end,
                                                                       HttpServletRequest request) {
        try {
            AccountRedis accountRedis = getAccount(request);
            if (accountRedis == null)
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<StatisticInfo>>builder()
                                .message(NOT_FOUND_ACCOUNT)
                                .build()
                );
            if (!accountRedis.getRole().getName().equals("ADMIN"))
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<StatisticInfo>>builder()
                                .message(NOT_OWNER)
                                .build()
                );
            LocalDateTime[] date = getStartAndEnd(start, end);
            Page<StatisticInfo> dtos = statisticsFacade.getAllStatistics(page, size, date[0], date[1]);
            if (dtos.isEmpty()){
                return ResponseEntity.badRequest().body(
                        ResponseObject.<Page<StatisticInfo>>builder()
                                .message("Khong tim thay hoa don nao trong khoang " + date[0] + " den " + date[1])
                                .build()
                );
            } else
                return ResponseEntity.ok().body(
                        ResponseObject.<Page<StatisticInfo>>builder()
                                .data(dtos)
                                .message("Da tim thay cac hoa don trong khoang " + date[0] + " den " + date[1])
                                .isSuccess(true)
                                .build()
                );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(
                    ResponseObject.<Page<StatisticInfo>>builder()
                            .message(SERVER_ERROR)
                            .build()
            );
        }
    }

    private LocalDateTime[] getStartAndEnd(String startStr, String endStr) {
        startStr = initDate(startStr);
        endStr = initDate(endStr);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime[] date = new LocalDateTime[2];
        if (!Objects.equals(startStr, "null") && !Objects.equals(endStr, "null")) {
            date[0] = LocalDate.parse(startStr, format).atStartOfDay();
            date[1] = LocalDate.parse(endStr, format).atStartOfDay();
        } else if (!Objects.equals(startStr, "null") && Objects.equals(endStr, "null")) {
            date[0] = LocalDate.parse(startStr, format).atStartOfDay();
            date[1] = date[0].plusMonths(1);
        } else if (Objects.equals(startStr, "null") && !Objects.equals(endStr, "null")) {
            date[0] = LocalDate.parse(endStr, format).atStartOfDay();
            date[1] = date[1].minusMonths(1);
        } else {
            date[0] = LocalDateTime.now().minusMonths(1);
            date[1] = LocalDateTime.now();
        }
        date[0] = date[0].with(LocalTime.MIN);
        date[1] = date[1].with(LocalTime.MAX);
        return date;
    }
    private String initDate(String str){
        if (str.equals("null")) return str;
        String[] startArr = str.split("/");
        startArr[0] = startArr[0].length() < 2 ? startArr[0] = "0" + startArr[0] : startArr[0];
        startArr[1] = startArr[1].length() < 2 ? startArr[1] = "0" + startArr[1] : startArr[1];
        return startArr[0] + "/" + startArr[1] + "/" + startArr[2];
    }
}
