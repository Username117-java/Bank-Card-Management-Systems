package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .roles(u.getRoles() == null ? null :
                        u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }
}