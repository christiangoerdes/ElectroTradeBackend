package com.goerdes.security;

import com.goerdes.security.auth.AuthService;
import com.goerdes.security.auth.RegisterRequest;
import com.goerdes.security.market.MarketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import static com.goerdes.security.user.Role.ADMIN;
import static com.goerdes.security.user.Role.MANAGER;

@SpringBootApplication
@EnableScheduling
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthService authService,
			MarketService marketService
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.name("Admin")
					.email("admin@mail.com")
					.password("password")
					.build();
			authService.register(admin, ADMIN);

			var manager = RegisterRequest.builder()
					.name("Admin")
					.email("manager@mail.com")
					.password("password")
					.build();
			authService.register(manager, MANAGER);

			marketService.createMarketEntity("Demo", List.of(
							230.0, 150.0, 180.0, 250.0, 120.0, 280.0, 190.0, 200.0, 220.0, 170.0,
					210.0, 290.0, 130.0, 240.0, 270.0, 160.0, 300.0, 110.0, 280.0, 170.0,
					250.0, 220.0, 190.0, 140.0, 200.0, 270.0, 150.0, 210.0, 260.0, 120.0
			));

		};
	}
}
