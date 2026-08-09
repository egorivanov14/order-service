package com.innowise.orderservice.dto.orderitem;

import com.innowise.orderservice.dto.item.ItemResponse;

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