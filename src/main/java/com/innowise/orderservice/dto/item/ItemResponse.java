package com.innowise.orderservice.dto.item;

import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String name,
        Long price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}