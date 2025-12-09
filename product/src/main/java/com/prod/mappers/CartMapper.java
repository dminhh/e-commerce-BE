package com.prod.mappers;

import com.prod.facades.data.CartProductInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    CartProductInfo getCPDTO(CartProductInfo dataDTO);
}
