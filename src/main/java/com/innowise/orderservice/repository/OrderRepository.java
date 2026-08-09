package com.innowise.orderservice.repository;

import com.innowise.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  /*
   * returns order with all items, without N+1
   * */
  @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
  Optional<Order> findById(Long id);

  Page<Order> findAllByUserId(Long userId, Pageable pageable);

  /*
   * returns all orders with all items, without N+1
   * */
  @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
  List<Order> findAllByIdIn(List<Long> ids);
}