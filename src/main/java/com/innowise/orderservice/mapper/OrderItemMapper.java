package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.item.ItemResponse;
import com.innowise.orderservice.dto.orderitem.OrderItemResponse;
import com.innowise.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  @Mapping(target = "id", source = "orderItem.id")
  @Mapping(target = "orderId", source = "orderItem.order.id")
  @Mapping(target = "item", source = "itemResponse")
  @Mapping(target = "createdAt", source = "orderItem.createdAt")
  @Mapping(target = "updatedAt", source = "orderItem.updatedAt")
  OrderItemResponse toOrderItemResponse(OrderItem orderItem, ItemResponse itemResponse);
}