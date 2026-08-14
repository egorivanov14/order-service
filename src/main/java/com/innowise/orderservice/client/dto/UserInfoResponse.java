package com.innowise.orderservice.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserInfoResponse(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}