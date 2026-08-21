package com.innowise.orderservice.client;

import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.userservice.grpc.UserServiceGrpc;
import com.innowise.userservice.grpc.UserServiceProto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
public class GrpcClientService {
  private static final Logger logger = LoggerFactory.getLogger(GrpcClientService.class);

  @GrpcClient("orderClientService")
  private UserServiceGrpc.UserServiceBlockingStub blockingStub;

  public UserInfoResponse getUserInfo(Long userId) {
    logger.debug("getUserInfo() called");
    UserServiceProto.GetUserInfoRequest request = UserServiceProto.GetUserInfoRequest.newBuilder()
            .setUserId(userId)
            .build();
    UserServiceProto.UserInfoResponse grpcResponse = blockingStub.getUserInfo(request);

    String birthDateString = grpcResponse.getBirthDate();
    String createdAtString = grpcResponse.getCreatedAt();
    String updatedAtString = grpcResponse.getUpdatedAt();
    LocalDate birthDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    try {
      birthDate = LocalDate.parse(birthDateString);
      createdAt = LocalDateTime.parse(createdAtString);
      updatedAt = LocalDateTime.parse(updatedAtString);
    } catch (DateTimeParseException | IllegalArgumentException e) {
      logger.error("Failed to getUserInfo(). Parse error: {} ", e.getMessage());
      throw new DataIntegrityViolationException(e.getMessage());
    }

    return new UserInfoResponse(
            grpcResponse.getId(),
            grpcResponse.getName(),
            grpcResponse.getSurname(),
            birthDate,
            grpcResponse.getEmail(),
            grpcResponse.getIsActive(),
            createdAt,
            updatedAt
    );
  }
}