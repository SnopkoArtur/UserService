package com.userservice.mapper;

import com.userservice.dto.CardDto;
import com.userservice.dto.UserDto;
import com.userservice.entity.PaymentCard;
import com.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User entity);
    User toEntity(UserDto dto);

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(CardDto dto);
}