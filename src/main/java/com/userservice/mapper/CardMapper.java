package com.userservice.mapper;

import com.userservice.dto.CardDto;
import com.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardDto toDto(PaymentCard entity);

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(CardDto dto);
}