package com.electrotrade;

import com.electrotrade.auth.AuthService;
import com.electrotrade.auth.RegisterRequest;
import com.electrotrade.market.MarketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.electrotrade.user.Role.ADMIN;
import static com.electrotrade.user.Role.MANAGER;

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

			createMarketWithRandomPrices(marketService, "StellarTech Inc.");
			createMarketWithRandomPrices(marketService, "QuantumEnergy Group");
			createMarketWithRandomPrices(marketService, "BioGen Innovations");
			createMarketWithRandomPrices(marketService, "CyberSafe Systems");
			createMarketWithRandomPrices(marketService, "EcoPower Solutions");
			createMarketWithRandomPrices(marketService, "AeroX Industries");
			createMarketWithRandomPrices(marketService, "HealthGenix Pharmaceuticals");
			createMarketWithRandomPrices(marketService, "GreenWave Renewables");
			createMarketWithRandomPrices(marketService, "DataDynamo Technologies");
			createMarketWithRandomPrices(marketService, "FinTech Futures Corp.");

		};
	}
	private void createMarketWithRandomPrices(
			MarketService marketService,
			String stockName
	) {
		List<Double> priceHistory = generateRandomPriceHistory();
		marketService.createMarketEntity(stockName, priceHistory);
	}

	private List<Double> generateRandomPriceHistory() {
		List<Double> priceHistory = new ArrayList<>();
		Random random = new Random();

		for (int i = 0; i < 30; i++) {
			boolean priceChange = random.nextBoolean();

			double priceChangeAmount = (random.nextDouble() - 0.5) * 20;

			double previousPrice = i > 0 ? priceHistory.get(i - 1) : 100.0;
			double newPrice = previousPrice + (priceChange ? priceChangeAmount : -priceChangeAmount);

			priceHistory.add(Math.round(newPrice * 100.0) / 100.0);
		}
		Collections.shuffle(priceHistory);
		return priceHistory;
	}
}
