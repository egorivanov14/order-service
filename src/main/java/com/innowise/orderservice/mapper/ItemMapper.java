package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.item.ItemResponse;
import com.innowise.orderservice.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
  ItemResponse toItemResponse(Item item);
}