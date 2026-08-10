package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.dto.orderitem.CreateOrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull
        Long userId,

        @NotNull
        @Valid
        List<CreateOrderItemRequest> orderItems
) {
}