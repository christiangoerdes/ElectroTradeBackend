package com.goerdes.security.auth;

import com.goerdes.security.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @NotNull
  private String name;
  @NotNull
  private String email;
  @NotNull
  private String password;
}
