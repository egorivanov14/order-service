package com.innowise.orderservice.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {

    registry.add(
            "spring.datasource.url",
            postgres::getJdbcUrl);

    registry.add(
            "spring.datasource.username",
            postgres::getUsername);

    registry.add(
            "spring.datasource.password",
            postgres::getPassword);
  }
}