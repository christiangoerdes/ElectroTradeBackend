package com.goerdes.security.config;

import com.goerdes.security.token.TokenRepo;
import com.goerdes.security.user.UserEntity;
import com.goerdes.security.user.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.naming.AuthenticationException;
import java.util.Arrays;

import static com.goerdes.security.user.Role.USER;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final LogoutHandler logoutHandler;
  private final JwtService jwtService;
  private final UserRepo userRepo;
  private final TokenRepo tokenRepo;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .exceptionHandling()
        .authenticationEntryPoint((request, response, authException) -> {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unauthorized");
        })
        .and()
        .cors(Customizer.withDefaults())
        .csrf()
        .disable()
        .authorizeHttpRequests()
        .requestMatchers(PathRequest.toH2Console()).permitAll()
        .requestMatchers(
            //    "/user/data/**",
                "/auth/**",
                "/h2-console/**",
                "/market/data"
        )
        .permitAll()
        .requestMatchers("/user/data/**").hasAnyRole(USER.name())
        .requestMatchers("/user/buy/**").hasAnyRole(USER.name())

       /* .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())

        .requestMatchers(GET, "/api/v1/admin/**").hasAuthority(ADMIN_READ.name())
        .requestMatchers(POST, "/api/v1/admin/**").hasAuthority(ADMIN_CREATE.name())
        .requestMatchers(PUT, "/api/v1/admin/**").hasAuthority(ADMIN_UPDATE.name())
        .requestMatchers(DELETE, "/api/v1/admin/**").hasAuthority(ADMIN_DELETE.name())*/

        .anyRequest()
        .authenticated()
        .and()
        .httpBasic(Customizer.withDefaults())
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .logout()
        .logoutUrl("/auth/logout")
        .addLogoutHandler(logoutHandler)
        .logoutSuccessHandler((request, response, authentication) -> {
          try {
            revokeAllUserTokens(extractUser(request));
          } catch (AuthenticationException e) {
            throw new RuntimeException(e);
          }
          SecurityContextHolder.clearContext();
        })
        .and()
        .headers().frameOptions().disable()
    ;

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  //TODO clean up/optimize code

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
}
