package com.innowise.orderservice.repository;

import com.innowise.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findById(Long id);

  List<Order> findAllByUserId(Long id);
}