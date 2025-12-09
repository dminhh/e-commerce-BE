package com.prod.facades;

import com.prod.facades.data.StatisticInfo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface IStatisticsFacade {
    Page<StatisticInfo> getAllStatistics(int page, int size, LocalDateTime start, LocalDateTime end);
}
