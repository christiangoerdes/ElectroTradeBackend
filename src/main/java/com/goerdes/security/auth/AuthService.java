package com.goerdes.security.auth;

import com.goerdes.security.config.JwtService;
import com.goerdes.security.token.Token;
import com.goerdes.security.token.TokenRepo;
import com.goerdes.security.token.TokenType;
import com.goerdes.security.user.Role;
import com.goerdes.security.user.UserEntity;
import com.goerdes.security.user.UserRepo;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepo userRepo;
  private final TokenRepo tokenRepo;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public ResponseEntity<AuthResponse> register(RegisterRequest request, Role role) {
    UserEntity user = createUser(request, role);
    var jwtToken = jwtService.generateToken(user);
    if(userRepo.findByEmail(request.getEmail()).isPresent()) {
      throw new EntityExistsException();
    }
    saveUserToken(userRepo.save(user), jwtToken);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Set-Cookie", "refreshToken=" + jwtService.generateRefreshToken(user) + "; HttpOnly; Path=/");

    return new ResponseEntity<>(
            AuthResponse.builder().accessToken(jwtToken).name(user.getName()).balance(user.getBalance()).role(user.getRole()).build(),
            responseHeaders,
            HttpStatus.CREATED
    );
  }
  private UserEntity createUser(RegisterRequest request, Role role) {
    return UserEntity.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .balance(100_000.00)
            .role(role)
            .build();
  }

  public ResponseEntity<AuthResponse> authenticate(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = userRepo.findByEmail(request.getEmail()).orElseThrow();
    var jwtToken = jwtService.generateToken(user);

    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Set-Cookie", "refreshToken=" + jwtService.generateRefreshToken(user) + "; HttpOnly; Path=/");

    return new ResponseEntity<>(
            AuthResponse.builder().accessToken(jwtToken).name(user.getName()).balance(user.getBalance()).role(user.getRole()).build(),
            responseHeaders ,
            HttpStatus.CREATED
    );
  }

  private void saveUserToken(UserEntity userEntity, String jwtToken) {
    var token = Token.builder()
        .userEntity(userEntity)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepo.save(token);
  }

  private void revokeAllUserTokens(UserEntity userEntity) {
    var validUserTokens = tokenRepo.findAllValidTokenByUser(userEntity.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepo.saveAll(validUserTokens);
  }

  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String refreshToken = getRefreshToken(request);
    if(refreshToken == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    final String userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.userRepo.findByEmail(userEmail).orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return new ResponseEntity<AuthResponse>(AuthResponse.builder()
                .accessToken(accessToken)
                .build(), HttpStatus.CREATED);
      }
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  private static String getRefreshToken(HttpServletRequest request) {
    String refreshToken = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }
    return refreshToken;
  }
}
