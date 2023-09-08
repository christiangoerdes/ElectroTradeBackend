package com.goerdes.security.auth;

import com.goerdes.security.user.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return service.register(request, Role.USER);
  }
  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(
      @RequestBody AuthRequest request
  ) {
    return service.authenticate(request);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    return service.refreshToken(request, response);
  }


}
