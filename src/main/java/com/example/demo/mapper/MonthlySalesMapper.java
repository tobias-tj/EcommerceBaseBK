package com.example.demo.mapper;

import com.example.demo.dto.MonthlySales;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MonthlySalesMapper {
    MonthlySalesMapper INSTANCE = Mappers.getMapper(MonthlySalesMapper.class);

    @Mapping(target = "monthName", source = "monthName")
    @Mapping(target = "quantitySales", source = "quantitySales")
    MonthlySales toMonthlySales( String monthName, Long quantitySales, Integer quantityObjective);
}
