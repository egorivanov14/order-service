package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.item.ItemResponse;
import com.innowise.orderservice.dto.orderitem.OrderItemResponse;
import com.innowise.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "item", source = "itemResponse")
  OrderItemResponse toOrderItemResponse(OrderItem orderItem, ItemResponse itemResponse);
}