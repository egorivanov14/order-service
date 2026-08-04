package com.innowise.orderservice.dto;

import java.time.LocalDateTime;

public record OrderItemResponse(
        Long id,
        Long orderId,
        ItemResponse item,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}