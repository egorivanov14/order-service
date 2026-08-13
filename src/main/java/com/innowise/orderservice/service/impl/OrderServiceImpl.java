package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.orderservice.dto.OrderStatus;
import com.innowise.orderservice.dto.item.ItemResponse;
import com.innowise.orderservice.dto.order.CreateOrderRequest;
import com.innowise.orderservice.dto.order.OrderResponse;
import com.innowise.orderservice.dto.order.UpdateOrderStatusRequest;
import com.innowise.orderservice.dto.orderitem.CreateOrderItemRequest;
import com.innowise.orderservice.dto.orderitem.OrderItemResponse;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
  private final ItemRepository itemRepository;
  private final UserServiceClient userServiceClient;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final ItemMapper itemMapper;

  public OrderServiceImpl(ItemRepository itemRepository, UserServiceClient userServiceClient, OrderRepository orderRepository, OrderMapper orderMapper, OrderItemMapper orderItemMapper, ItemMapper itemMapper) {
    this.itemRepository = itemRepository;
    this.userServiceClient = userServiceClient;
    this.orderRepository = orderRepository;
    this.orderMapper = orderMapper;
    this.orderItemMapper = orderItemMapper;
    this.itemMapper = itemMapper;
  }

  @Override
  @Transactional
  public OrderResponse create(CreateOrderRequest createOrderRequest) {
    Order order = new Order();
    Long userId = createOrderRequest.userId();
    order.setUserId(userId);
    order.setStatus(OrderStatus.CREATED);

    Long totalPrice = 0L;

    List<CreateOrderItemRequest> createOrderItemRequestList = createOrderRequest.orderItems();
    for (CreateOrderItemRequest createOrderItemRequest : createOrderItemRequestList) {
      OrderItem orderItem = new OrderItem();

      Long itemId = createOrderItemRequest.itemId();
      Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Item not found"));
      orderItem.setItem(item);

      Integer quantity = createOrderItemRequest.quantity();
      orderItem.setQuantity(quantity);

      orderItem.setOrder(order);
      order.addOrderItem(orderItem);

      Long itemPrice = item.getPrice();
      totalPrice += itemPrice * quantity;
    }
    order.setTotalPrice(totalPrice);

    Order savedOrder = orderRepository.save(order);
    return buildOrderResponse(savedOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
    return buildOrderResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAllByUserId(Long userId, Pageable pageable) {
    Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);

    return extractOrderResponsePage(pageable, orders);
  }

  @Override
  @Transactional
  public OrderResponse update(Long id, UpdateOrderStatusRequest updateOrderStatusRequest) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
    OrderStatus newStatus = updateOrderStatusRequest.status();
    order.setStatus(newStatus);

    Order updatedOrder = orderRepository.save(order);
    return buildOrderResponse(updatedOrder);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    orderRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAll(Pageable pageable) {
    Page<Order> orders = orderRepository.findAll(pageable);
    return extractOrderResponsePage(pageable, orders);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAllAndFilterByCreationDateAndStatus(Pageable pageable, LocalDateTime creationDate, OrderStatus orderStatus) {
    Specification<Order> creationDateSpecification = OrderSpecification.filterByCreationDate(creationDate);
    Specification<Order> statusSpecification = OrderSpecification.filterByStatus(orderStatus);
    Specification<Order> specification = Specification.where(creationDateSpecification).and(statusSpecification);

    Page<Order> orders = orderRepository.findAll(specification, pageable);
    return extractOrderResponsePage(pageable, orders);
  }

  private Page<OrderResponse> extractOrderResponsePage(Pageable pageable, Page<Order> orders) {
    List<Long> ids = orders.stream().map(Order::getId).toList();
    List<Order> fullOrders = orderRepository.findAllByIdIn(ids);
    List<OrderResponse> orderResponses = fullOrders.stream().map(this::buildOrderResponse).toList();
    return new PageImpl<>(orderResponses, pageable, orders.getTotalElements());
  }

  private OrderResponse buildOrderResponse(Order order) {
    Long userId = order.getUserId();
    UserInfoResponse userInfoResponse = userServiceClient.getUserInfo(userId);

    List<OrderItem> orderItems = order.getOrderItems();
    List<OrderItemResponse> orderItemResponses = orderItems
            .stream().map(
                    orderItem -> {
                      Item item = orderItem.getItem();
                      ItemResponse itemResponse = itemMapper.toItemResponse(item);
                      return orderItemMapper.toOrderItemResponse(orderItem, itemResponse);
                    }
            ).toList();

    return orderMapper.toOrderResponse(order, orderItemResponses, userInfoResponse);
  }
}