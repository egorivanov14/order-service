package com.innowise.orderservice.mapper;

import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.orderservice.dto.order.OrderResponse;
import com.innowise.orderservice.dto.orderitem.OrderItemResponse;
import com.innowise.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "id", source = "order.id")
  @Mapping(target = "userInfo", source = "userInfo")
  @Mapping(target = "items", source = "orderItems")
  @Mapping(target = "createdAt", source = "order.createdAt")
  @Mapping(target = "updatedAt", source = "order.updatedAt")
  OrderResponse toOrderResponse(Order order, List<OrderItemResponse> orderItems, UserInfoResponse userInfo);
}