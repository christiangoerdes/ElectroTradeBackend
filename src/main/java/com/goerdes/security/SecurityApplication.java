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

			marketService.createMarketEntity("Market 1", List.of(
					100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0, 135.0, 140.0, 145.0,
					150.0, 155.0, 160.0, 165.0, 170.0, 175.0, 180.0, 185.0, 190.0, 195.0
			));

			marketService.createMarketEntity("Market 2", List.of(
					50.0, 55.0, 60.0, 65.0, 70.0, 75.0, 80.0, 85.0, 90.0, 95.0,
					100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0, 135.0, 140.0, 145.0
			));

			marketService.createMarketEntity("Market 3", List.of(
					200.0, 205.0, 210.0, 215.0, 220.0, 225.0, 230.0, 235.0, 240.0, 245.0,
					250.0, 255.0, 260.0, 265.0, 270.0, 275.0, 280.0, 285.0, 290.0, 295.0
			));

		};
	}
}
