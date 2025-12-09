package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.LabelInfo;
import com.prod.models.products.Label;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface ILabelFacade {
    ResponseObject<Label> createLabel(LabelInfo label);
    ResponseObject<Label> getLabel(int id);
    ResponseObject<Page<Label>> getAllLabels(int page, int limit, String field, String direction);
    ResponseObject<Page<Label>> getAllLabelsByName(String name, int page, int limit, String field, String direction);
//    ResponseObject<Boolean> deleteLabel(LabelDTO label);
    ResponseObject<Label> updateLabel(LabelInfo label);
}
