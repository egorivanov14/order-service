package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.orderservice.dto.orderitem.OrderItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String status,
        Long totalPrice,
        boolean deleted,
        List<OrderItemResponse> items,
        UserInfoResponse userInfo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) {
}