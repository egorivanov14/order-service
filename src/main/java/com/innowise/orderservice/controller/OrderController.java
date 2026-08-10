package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.OrderStatus;
import com.innowise.orderservice.dto.order.CreateOrderRequest;
import com.innowise.orderservice.dto.order.OrderResponse;
import com.innowise.orderservice.dto.order.UpdateOrderStatusRequest;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.innowise.orderservice.configuration.Constants.CREATED_AT;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/create")
  public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
    OrderResponse orderResponse = orderService.create(request);
    return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
    OrderResponse orderResponse = orderService.findById(id);
    return ResponseEntity.ok(orderResponse);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<OrderResponse>> findAllByUserId(
          @PathVariable Long userId,
          @PageableDefault(size = 20, sort = CREATED_AT, direction = Sort.Direction.DESC) Pageable pageable) {
    Page<OrderResponse> orderResponsePage = orderService.findAllByUserId(userId, pageable);
    return ResponseEntity.ok(orderResponsePage);
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateStatus(
          @PathVariable Long id,
          @Valid @RequestBody UpdateOrderStatusRequest updateOrderStatusRequest) {
    OrderResponse orderResponse = orderService.update(id, updateOrderStatusRequest);
    return ResponseEntity.ok(orderResponse);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<Page<OrderResponse>> findAll(
          @PageableDefault(size = 20, sort = CREATED_AT, direction = Sort.Direction.DESC) Pageable pageable) {
    Page<OrderResponse> orderResponsePage = orderService.findAll(pageable);
    return ResponseEntity.ok(orderResponsePage);
  }

  @GetMapping("/filter")
  public ResponseEntity<Page<OrderResponse>> findAllAndFilterByCreationDateAndStatus(
          @PageableDefault(size = 20, sort = CREATED_AT, direction = Sort.Direction.DESC) Pageable pageable,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime creationDate,
          @RequestParam(required = false) OrderStatus orderStatus) {
    Page<OrderResponse> orderResponsePage = orderService.findAllAndFilterByCreationDateAndStatus(pageable, creationDate, orderStatus);
    return ResponseEntity.ok(orderResponsePage);
  }
}