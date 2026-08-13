package com.innowise.orderservice;

import com.innowise.orderservice.dto.OrderStatus;

import java.time.LocalDateTime;

public class TestConstants {
  public static final Long ORDER_ID = 1L;
  public static final Long USER_ID = 10L;
  public static final Long ITEM_ID = 100L;
  public static final Long ITEM_PRICE = 500L;
  public static final Integer QUANTITY = 2;
  public static final Long EXPECTED_TOTAL_PRICE = 1000L;
  public static final String ITEM_NAME = "Keyboard";
  public static final OrderStatus INITIAL_STATUS = OrderStatus.CREATED;
  public static final OrderStatus NEW_STATUS = OrderStatus.PAID;
  public static final LocalDateTime CREATION_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);
  public static final String USER_EMAIL = "testmail@gmail.com";
  public static final String USER_NAME = "testuser";
  public static final String USER_SURNAME = "testusersurname";
}