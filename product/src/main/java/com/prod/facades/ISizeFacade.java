package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.SizeInfo;
import com.prod.models.carts.Size;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ISizeFacade {
    ResponseObject<Size> createSize(SizeInfo sizeInfo);
    ResponseObject<Size> updateSize(SizeInfo sizeInfo);
    ResponseObject<Size> getSizeById(int id);
    ResponseObject<List<Size>> getAllSizes();
}
