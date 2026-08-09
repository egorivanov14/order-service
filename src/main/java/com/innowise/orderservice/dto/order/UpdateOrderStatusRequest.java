package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.dto.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull
        OrderStatus status
) {
}