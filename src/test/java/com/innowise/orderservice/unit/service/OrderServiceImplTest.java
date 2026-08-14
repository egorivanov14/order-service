package com.innowise.orderservice.unit.service;

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
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static com.innowise.orderservice.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
  @Mock
  private ItemRepository itemRepository;
  @Mock
  private GrpcClientService grpcClientService;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private OrderMapper orderMapper;
  @Mock
  private OrderItemMapper orderItemMapper;
  @Mock
  private ItemMapper itemMapper;

  @InjectMocks
  private OrderServiceImpl orderService;

  @Test
  void create_shouldCreateOrder_andPersistCorrectFields() {
    CreateOrderItemRequest itemRequest = new CreateOrderItemRequest(ITEM_ID, QUANTITY);
    CreateOrderRequest request = new CreateOrderRequest(USER_ID, List.of(itemRequest));

    Item item = new Item();
    item.setId(ITEM_ID);
    item.setName(ITEM_NAME);
    item.setPrice(ITEM_PRICE);

    ItemResponse itemResponse = mock(ItemResponse.class);
    UserInfoResponse userInfoResponse = mock(UserInfoResponse.class);
    OrderItemResponse orderItemResponse = mock(OrderItemResponse.class);
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    when(grpcClientService.getUserInfo(USER_ID)).thenReturn(userInfoResponse);
    when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);
    when(orderItemMapper.toOrderItemResponse(any(OrderItem.class), eq(itemResponse)))
            .thenReturn(orderItemResponse);
    when(orderMapper.toOrderResponse(any(Order.class), eq(List.of(orderItemResponse)), eq(userInfoResponse)))
            .thenReturn(expectedResponse);

    OrderResponse actual = orderService.create(request);

    assertEquals(expectedResponse, actual);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());
    Order savedOrder = orderCaptor.getValue();

    assertEquals(USER_ID, savedOrder.getUserId());
    assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
    assertEquals(EXPECTED_TOTAL_PRICE, savedOrder.getTotalPrice());
    assertEquals(1, savedOrder.getOrderItems().size());
  }

  @Test
  void create_multipleItems_shouldSumTotalPriceCorrectly() {
    CreateOrderItemRequest req1 = new CreateOrderItemRequest(ITEM_ID, 2);
    CreateOrderItemRequest req2 = new CreateOrderItemRequest(200L, 3);
    CreateOrderRequest request = new CreateOrderRequest(USER_ID, List.of(req1, req2));

    Item item1 = new Item();
    item1.setId(ITEM_ID);
    item1.setPrice(500L);

    Item item2 = new Item();
    item2.setId(200L);
    item2.setPrice(100L);

    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item1));
    when(itemRepository.findById(200L)).thenReturn(Optional.of(item2));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    when(grpcClientService.getUserInfo(USER_ID)).thenReturn(mock(UserInfoResponse.class));
    when(itemMapper.toItemResponse(any(Item.class))).thenReturn(mock(ItemResponse.class));
    when(orderItemMapper.toOrderItemResponse(any(OrderItem.class), any(ItemResponse.class)))
            .thenReturn(mock(OrderItemResponse.class));
    when(orderMapper.toOrderResponse(any(Order.class), anyList(), any(UserInfoResponse.class)))
            .thenReturn(mock(OrderResponse.class));

    orderService.create(request);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());

    assertEquals(1300L, orderCaptor.getValue().getTotalPrice());
  }

  @Test
  void create_itemNotFound_shouldThrowNotFoundException() {
    CreateOrderItemRequest itemRequest = new CreateOrderItemRequest(ITEM_ID, QUANTITY);
    CreateOrderRequest request = new CreateOrderRequest(USER_ID, List.of(itemRequest));

    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> orderService.create(request));

    verify(orderRepository, never()).save(any());
  }

  @Test
  void findById_shouldReturnOrder() {
    Order order = buildOrderWithOneItem();
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    mockBuildOrderResponseChain(order, expectedResponse);

    OrderResponse actual = orderService.findById(ORDER_ID);

    assertEquals(expectedResponse, actual);
  }

  @Test
  void findById_notFound_shouldThrowNotFoundException() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> orderService.findById(ORDER_ID));
  }

  @Test
  void findAllByUserId_shouldReturnPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Order order = buildOrderWithOneItem();
    Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(orderRepository.findAllByUserId(USER_ID, pageable)).thenReturn(page);
    when(orderRepository.findAllByIdIn(List.of(ORDER_ID))).thenReturn(List.of(order));
    mockBuildOrderResponseChain(order, expectedResponse);

    Page<OrderResponse> result = orderService.findAllByUserId(USER_ID, pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(expectedResponse, result.getContent().getFirst());
  }

  @Test
  void update_shouldChangeStatus() {
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(NEW_STATUS);
    Order order = buildOrderWithOneItem();
    order.setStatus(INITIAL_STATUS);
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);
    mockBuildOrderResponseChain(order, expectedResponse);

    OrderResponse actual = orderService.update(ORDER_ID, request);

    assertEquals(expectedResponse, actual);
    assertEquals(NEW_STATUS, order.getStatus());
  }

  @Test
  void update_notFound_shouldThrowNotFoundException() {
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(NEW_STATUS);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> orderService.update(ORDER_ID, request));

    verify(orderRepository, never()).save(any());
  }

  @Test
  void delete_shouldDeleteOrder() {
    doNothing().when(orderRepository).deleteById(ORDER_ID);

    orderService.delete(ORDER_ID);

    verify(orderRepository).deleteById(ORDER_ID);
  }

  @Test
  void findAll_shouldReturnPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Order order = buildOrderWithOneItem();
    Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(orderRepository.findAll(pageable)).thenReturn(page);
    when(orderRepository.findAllByIdIn(List.of(ORDER_ID))).thenReturn(List.of(order));
    mockBuildOrderResponseChain(order, expectedResponse);

    Page<OrderResponse> result = orderService.findAll(pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(expectedResponse, result.getContent().getFirst());
  }

  @Test
  void findAllAndFilterByCreationDateAndStatus_shouldReturnFilteredPage() {
    Pageable pageable = PageRequest.of(0, 10);
    Order order = buildOrderWithOneItem();
    Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
    OrderResponse expectedResponse = mock(OrderResponse.class);

    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(orderRepository.findAllByIdIn(List.of(ORDER_ID))).thenReturn(List.of(order));
    mockBuildOrderResponseChain(order, expectedResponse);

    Page<OrderResponse> result =
            orderService.findAllAndFilterByCreationDateAndStatus(pageable, CREATION_DATE, NEW_STATUS);

    assertEquals(1, result.getTotalElements());
    assertEquals(expectedResponse, result.getContent().getFirst());

    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
  }

  private Order buildOrderWithOneItem() {
    Order order = new Order();
    order.setId(ORDER_ID);
    order.setUserId(USER_ID);
    order.setStatus(INITIAL_STATUS);
    order.setTotalPrice(EXPECTED_TOTAL_PRICE);

    Item item = new Item();
    item.setId(ITEM_ID);
    item.setName(ITEM_NAME);
    item.setPrice(ITEM_PRICE);

    OrderItem orderItem = new OrderItem();
    orderItem.setId(1L);
    orderItem.setItem(item);
    orderItem.setQuantity(QUANTITY);
    orderItem.setOrder(order);

    order.addOrderItem(orderItem);
    return order;
  }

  private void mockBuildOrderResponseChain(Order order, OrderResponse expectedResponse) {
    UserInfoResponse userInfoResponse = mock(UserInfoResponse.class);
    ItemResponse itemResponse = mock(ItemResponse.class);
    OrderItemResponse orderItemResponse = mock(OrderItemResponse.class);

    when(grpcClientService.getUserInfo(order.getUserId())).thenReturn(userInfoResponse);
    when(itemMapper.toItemResponse(any(Item.class))).thenReturn(itemResponse);
    when(orderItemMapper.toOrderItemResponse(any(OrderItem.class), eq(itemResponse)))
            .thenReturn(orderItemResponse);
    when(orderMapper.toOrderResponse(eq(order), anyList(), eq(userInfoResponse)))
            .thenReturn(expectedResponse);
  }
}