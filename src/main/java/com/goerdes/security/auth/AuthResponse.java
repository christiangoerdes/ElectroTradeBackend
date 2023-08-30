package com.goerdes.security.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.goerdes.security.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

  @JsonProperty("accessToken")
  private String accessToken;

  @JsonProperty("name")
  private String name;

  @JsonProperty("balance")
  private Double balance;

  @JsonProperty("role")
  private Role role;
}
