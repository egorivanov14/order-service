package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.OrderStatus;
import com.innowise.orderservice.dto.order.CreateOrderRequest;
import com.innowise.orderservice.dto.order.OrderResponse;
import com.innowise.orderservice.dto.order.UpdateOrderStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {
  OrderResponse create(CreateOrderRequest createOrderRequest);

  OrderResponse findById(Long id);

  Page<OrderResponse> findAllByUserId(Long userId, Pageable pageable);

  OrderResponse update(Long id, UpdateOrderStatusRequest updateOrderStatusRequest);

  void delete(Long id);

  Page<OrderResponse> findAll(Pageable pageable);

  Page<OrderResponse> findAllAndFilterByCreationDateAndStatus(Pageable pageable, LocalDateTime creationDate, OrderStatus orderStatus);
}