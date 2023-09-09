package com.electrotrade.auth;

import com.electrotrade.config.JwtService;
import com.electrotrade.token.Token;
import com.electrotrade.token.TokenRepo;
import com.electrotrade.token.TokenType;
import com.electrotrade.user.Role;
import com.electrotrade.user.UserEntity;
import com.electrotrade.user.UserRepo;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

import static com.electrotrade.token.TokenType.BEARER;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepo userRepo;
  private final TokenRepo tokenRepo;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public ResponseEntity<com.electrotrade.auth.AuthResponse> register(com.electrotrade.auth.RegisterRequest request, Role role) {
    UserEntity user = createUser(request, role);
    var jwtToken = jwtService.generateToken(user);
    if(userRepo.findByEmail(request.getEmail()).isPresent()) {
      throw new EntityExistsException();
    }
    String refreshToken =  jwtService.generateRefreshToken(user);
    UserEntity userEntity = userRepo.save(user);
    saveUserToken(userEntity, jwtToken);
    saveRefreshToken(userEntity, refreshToken);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Set-Cookie", "refreshToken=" + refreshToken + "; HttpOnly; Path=/");

    return new ResponseEntity<>(
            com.electrotrade.auth.AuthResponse.builder().accessToken(jwtToken).name(user.getName()).balance(user.getBalance()).role(user.getRole()).build(),
            responseHeaders,
            HttpStatus.CREATED
    );
  }
  private UserEntity createUser(com.electrotrade.auth.RegisterRequest request, Role role) {
    return UserEntity.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .balance(100_000.00)
            .role(role)
            .build();
  }

  public ResponseEntity<com.electrotrade.auth.AuthResponse> authenticate(com.electrotrade.auth.AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = userRepo.findByEmail(request.getEmail()).orElseThrow();
    String refreshToken =  jwtService.generateRefreshToken(user);
    String jwtToken = jwtService.generateToken(user);
    saveRefreshToken(user, refreshToken);

    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Set-Cookie", "refreshToken=" + refreshToken + "; HttpOnly; Path=/");

    return new ResponseEntity<>(
            com.electrotrade.auth.AuthResponse.builder().accessToken(jwtToken).name(user.getName()).balance(user.getBalance()).role(user.getRole()).build(),
            responseHeaders ,
            HttpStatus.CREATED
    );
  }

  private void saveUserToken(UserEntity userEntity, String jwtToken) {
    Token token = Token.builder()
        .userEntity(userEntity)
        .token(jwtToken)
        .tokenType(BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepo.save(token);
  }

  private void saveRefreshToken(UserEntity userEntity, String jwtToken) {
    Token token = Token.builder()
            .userEntity(userEntity)
            .token(jwtToken)
            .tokenType(TokenType.REFRESH)
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
      if (BEARER.equals(token.getTokenType())) {
        token.setExpired(true);
        token.setRevoked(true);
      }
    });
    tokenRepo.saveAll(validUserTokens);
  }

  public ResponseEntity<?> refreshToken(HttpServletRequest request) {

    String refreshToken = getRefreshToken(request);
    if(refreshToken == null ||tokenRepo.existsByTokenAndRevoked(refreshToken, true)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    final String userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.userRepo.findByEmail(userEmail).orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return new ResponseEntity<>(com.electrotrade.auth.AuthResponse.builder()
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

  public void invalidateToken(HttpServletRequest request) throws AuthenticationException {
    UserEntity user = extractUser(request);
    revokeAllUserTokens(user);
  }

  private UserEntity extractUser(HttpServletRequest request) throws AuthenticationException {
    return userRepo.findByEmail(jwtService.extractUsername(extractJWT(request))).orElseThrow();
  }

  private String extractJWT(HttpServletRequest request) throws AuthenticationException {
    String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    } else {
      throw new AuthenticationException();
    }
  }
}
