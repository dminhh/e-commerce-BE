package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.OverviewInfo;
import com.prod.models.products.Overview;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface IOverviewFacade {
    ResponseObject<Overview> createOverview(OverviewInfo overview);
    ResponseObject<Overview> getOverview(OverviewInfo overview);
    ResponseObject<Page<Overview>> getAllOverviewsByProductId(OverviewInfo overviewInfo, int page, int size, String field, String direct);
    ResponseObject<Page<Overview>> getAllOverviewsByUserId(OverviewInfo overviewInfo, int page, int size, String field, String direct);
}
