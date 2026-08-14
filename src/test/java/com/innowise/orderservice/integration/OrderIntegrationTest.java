package com.innowise.orderservice.integration;

import com.innowise.orderservice.client.GrpcClientService;
import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.orderservice.dto.OrderStatus;
import com.innowise.orderservice.dto.order.CreateOrderRequest;
import com.innowise.orderservice.dto.order.UpdateOrderStatusRequest;
import com.innowise.orderservice.dto.orderitem.CreateOrderItemRequest;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static com.innowise.orderservice.TestConstants.*;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderIntegrationTest extends TestcontainersConfiguration {

  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private ItemRepository itemRepository;

  @MockitoBean
  private GrpcClientService grpcClientService;

  private MockMvc mockMvc;
  private Long itemId;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    Item item = new Item();
    item.setName(ITEM_NAME);
    item.setPrice(ITEM_PRICE);
    itemId = itemRepository.save(item).getId();

    UserInfoResponse userInfoResponse =
            new UserInfoResponse(USER_ID, USER_NAME, USER_SURNAME, null, USER_EMAIL, TRUE, null, null);
    when(grpcClientService.getUserInfo(USER_ID)).thenReturn(userInfoResponse);
  }

  @AfterEach
  void clearAll() {
    orderRepository.deleteAll();
    itemRepository.deleteAll();
  }

  @Test
  void create_shouldCreateOrder() throws Exception {
    CreateOrderRequest request =
            new CreateOrderRequest(USER_ID, List.of(new CreateOrderItemRequest(itemId, QUANTITY)));

    mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(USER_ID))
            .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
            .andExpect(jsonPath("$.totalPrice").value(EXPECTED_TOTAL_PRICE))
            .andExpect(jsonPath("$.items[0].quantity").value(QUANTITY))
            .andExpect(jsonPath("$.items[0].item.name").value(ITEM_NAME))
            .andExpect(jsonPath("$.userInfo.email").value(USER_EMAIL));
  }

  @Test
  void create_itemNotFound_shouldReturnNotFound() throws Exception {
    CreateOrderRequest request =
            new CreateOrderRequest(USER_ID, List.of(new CreateOrderItemRequest(999_999L, QUANTITY)));

    mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
  }

  @Test
  void create_missingUserId_shouldReturnBadRequest() throws Exception {
    String json = """
            {"orderItems":[{"itemId":%d,"quantity":1}]}
            """.formatted(itemId);

    mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isBadRequest());
  }

  @Test
  void create_negativeQuantity_shouldReturnBadRequest() throws Exception {
    String json = """
            {"userId":%d,"orderItems":[{"itemId":%d,"quantity":-1}]}
            """.formatted(USER_ID, itemId);

    mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isBadRequest());
  }

  @Test
  void findById_shouldReturnOrder() throws Exception {
    Long orderId = createOrderAndGetId();

    mockMvc.perform(get("/api/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.userInfo.email").value(USER_EMAIL));
  }

  @Test
  void findById_notFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/orders/{id}", 999_999L))
            .andExpect(status().isNotFound());
  }

  @Test
  void findAllByUserId_shouldReturnOrders() throws Exception {
    createOrderAndGetId();

    mockMvc.perform(get("/api/orders/user/{userId}", USER_ID)
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].userId").value(USER_ID));
  }

  @Test
  void findAllByUserId_noOrders_shouldReturnEmptyPage() throws Exception {
    mockMvc.perform(get("/api/orders/user/{userId}", 555_555L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void updateStatus_shouldUpdateStatus() throws Exception {
    Long orderId = createOrderAndGetId();
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELED);

    mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(OrderStatus.CANCELED.name()));

    Order updated = orderRepository.findById(orderId).orElseThrow();
    assertEquals(OrderStatus.CANCELED, updated.getStatus());
  }

  @Test
  void updateStatus_notFound_shouldReturn404() throws Exception {
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELED);

    mockMvc.perform(patch("/api/orders/{id}/status", 999_999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
  }

  @Test
  void updateStatus_missingStatus_shouldReturnBadRequest() throws Exception {
    Long orderId = createOrderAndGetId();

    mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void delete_shouldDeleteOrder() throws Exception {
    Long orderId = createOrderAndGetId();

    mockMvc.perform(delete("/api/orders/{id}", orderId))
            .andExpect(status().isNoContent());

    Optional<Order> deleted = orderRepository.findById(orderId);
    assertTrue(deleted.isEmpty());
  }

  @Test
  void findAll_shouldReturnAllOrders() throws Exception {
    createOrderAndGetId();

    mockMvc.perform(get("/api/orders")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  void filter_byStatus_shouldReturnMatchingOrders() throws Exception {
    createOrderAndGetId();

    mockMvc.perform(get("/api/orders/filter")
                    .param("orderStatus", OrderStatus.CREATED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value(OrderStatus.CREATED.name()));
  }

  @Test
  void filter_byStatus_noMatch_shouldReturnEmptyPage() throws Exception {
    createOrderAndGetId();

    mockMvc.perform(get("/api/orders/filter")
                    .param("orderStatus", OrderStatus.CANCELED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
  }

  private Long createOrderAndGetId() throws Exception {
    CreateOrderRequest request =
            new CreateOrderRequest(USER_ID, List.of(new CreateOrderItemRequest(itemId, QUANTITY)));

    String response = mockMvc.perform(post("/api/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

    return objectMapper.readTree(response).get("id").asLong();
  }
}