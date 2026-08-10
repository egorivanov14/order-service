package com.innowise.orderservice.specification;

import com.innowise.orderservice.dto.OrderStatus;
import com.innowise.orderservice.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {
  public static Specification<Order> filterByCreationDate(LocalDateTime creationDate) {
    return((root, query, criteriaBuilder) ->
            creationDate == null ? null : criteriaBuilder.equal(root.get("creationDate"), creationDate));
  }

  public static Specification<Order> filterByStatus(OrderStatus status) {
    return((root, query, criteriaBuilder) ->
            status == null ? null : criteriaBuilder.equal(root.get("status"), status));
  }
}