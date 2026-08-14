package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.GrpcClientService;
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
import com.innowise.orderservice.exception.UserServiceException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specification.OrderSpecification;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
  private final ItemRepository itemRepository;
  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final ItemMapper itemMapper;
  private final GrpcClientService grpcClientService;

  public OrderServiceImpl(ItemRepository itemRepository, OrderRepository orderRepository, OrderMapper orderMapper, OrderItemMapper orderItemMapper, ItemMapper itemMapper, GrpcClientService grpcClientService) {
    this.itemRepository = itemRepository;
    this.orderRepository = orderRepository;
    this.orderMapper = orderMapper;
    this.orderItemMapper = orderItemMapper;
    this.itemMapper = itemMapper;
    this.grpcClientService = grpcClientService;
  }

  @Override
  @Transactional
  public OrderResponse create(CreateOrderRequest createOrderRequest) {
    logger.debug("create() called");
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
    logger.info("Order created: orderId = {}", savedOrder.getId());
    return buildOrderResponse(savedOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    logger.debug("findById() called");
    Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
    return buildOrderResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAllByUserId(Long userId, Pageable pageable) {
    logger.debug("findAllByUserId() called: userId = {}", userId);
    Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);
    return extractOrderResponsePage(pageable, orders);
  }

  @Override
  @Transactional
  public OrderResponse update(Long id, UpdateOrderStatusRequest updateOrderStatusRequest) {
    logger.debug("update() called: orderId = {}", id);
    Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
    OrderStatus newStatus = updateOrderStatusRequest.status();
    order.setStatus(newStatus);

    Order updatedOrder = orderRepository.save(order);
    return buildOrderResponse(updatedOrder);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    logger.debug("delete() called");
    orderRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAll(Pageable pageable) {
    logger.debug("findAll() called");
    Page<Order> orders = orderRepository.findAll(pageable);
    return extractOrderResponsePage(pageable, orders);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> findAllAndFilterByCreationDateAndStatus(Pageable pageable, LocalDateTime creationDate, OrderStatus orderStatus) {
    logger.debug("findAllAndFilterByCreationDateAndStatus() called");
    Specification<Order> creationDateSpecification = OrderSpecification.filterByCreationDate(creationDate);
    Specification<Order> statusSpecification = OrderSpecification.filterByStatus(orderStatus);
    Specification<Order> specification = Specification.where(creationDateSpecification).and(statusSpecification);

    Page<Order> orders = orderRepository.findAll(specification, pageable);
    return extractOrderResponsePage(pageable, orders);
  }

  private Page<OrderResponse> extractOrderResponsePage(Pageable pageable, Page<Order> orders) {
    logger.info("extractOrderResponsePage() called");
    List<Long> ids = orders.stream().map(Order::getId).toList();
    List<Order> fullOrders = orderRepository.findAllByIdIn(ids);
    List<OrderResponse> orderResponses = fullOrders.stream().map(this::buildOrderResponse).toList();
    return new PageImpl<>(orderResponses, pageable, orders.getTotalElements());
  }

  private OrderResponse buildOrderResponse(Order order) {
    Long userId = order.getUserId();
    UserInfoResponse userInfoResponse;
    try {
      userInfoResponse = grpcClientService.getUserInfo(userId);
    } catch (StatusRuntimeException e) {
      throw new UserServiceException(e.getMessage());
    }

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