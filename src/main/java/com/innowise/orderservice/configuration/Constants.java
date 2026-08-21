package com.innowise.orderservice.configuration;

import io.grpc.Metadata;

public class Constants {
  public static final String CREATED_AT = "createdAt";
  public static final String STATUS = "status";
  public static final String SERVICE_SECRET_KEY_NAME_CONST = "serviceSecretKey";
  public static final Metadata.Key<String> SERVICE_SECRET_KEY_CONST =
          Metadata.Key.of(SERVICE_SECRET_KEY_NAME_CONST, Metadata.ASCII_STRING_MARSHALLER);
}