package com.innowise.orderservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserInfoResponse(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}