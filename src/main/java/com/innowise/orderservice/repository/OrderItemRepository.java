package com.innowise.orderservice.repository;

import com.innowise.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findAllByOrderId(Long orderId);
}