package com.innowise.orderservice.client;

import com.innowise.orderservice.client.dto.UserInfoResponse;
import com.innowise.orderservice.exception.UserServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserServiceClient {
  private final RestClient userServiceRestClient;
  private final String getUri;

  public UserServiceClient(RestClient userServiceRestClient,  @Value("${services.user-service.get-uri}") String getUri) {
    this.userServiceRestClient = userServiceRestClient;
    this.getUri = getUri;
  }

  public UserInfoResponse getUserInfo(Long id){
    return userServiceRestClient.get()
            .uri(getUri, id)
            .retrieve()
            .onStatus(HttpStatusCode::isError,
                    ((request, response) -> {
                      throw new UserServiceException("Error getting user info");
                    }))
            .toEntity(UserInfoResponse.class)
            .getBody();
  }
}