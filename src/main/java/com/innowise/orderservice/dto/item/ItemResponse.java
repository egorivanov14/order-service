package com.innowise.orderservice.dto;

import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String name,
        Long price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}