package com.goerdes.security.market;

import com.goerdes.security.config.JwtService;
import com.goerdes.security.user.UserEntity;
import com.goerdes.security.user.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final UserRepo userRepo;
    private final MarketRepo marketRepo;
    private final JwtService jwtService;

    public void createMarketEntity(String name, List<Double> priceHistory) {
        marketRepo.save(MarketEntity.builder().name(name).priceHistory(priceHistory).build());
    }

    public StockList getAllStocks() {
        updateAllEntities();
        return StockList.builder().stocks(marketRepo.findAll()).timestamps(getLast30DaysFormatted()).build();
    }

    private static List<String> getLast30DaysFormatted() {
        List<String> formattedDates = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");

        for (int i = 0; i < 30; i++) {
            formattedDates.add(currentDate.format(dateFormatter).replace(".", ""));
            currentDate = currentDate.minusDays(1);
        }
        Collections.reverse(formattedDates);
        return formattedDates;
    }

    @Scheduled(cron = "0 0 * * * *")
    private void updateAllEntities() {
        List<MarketEntity> marketEntities = marketRepo.findAll();

        for (MarketEntity entity : marketEntities) {
            entity.updatePriceHistory();
        }
        marketRepo.saveAll(marketEntities);
    }

    public ResponseEntity<String> buy(HttpServletRequest request, Integer marketId, int quantity) throws AuthenticationException {
        UserEntity user = extractUser(request);
        try {
            MarketEntity stock = marketRepo.findById(marketId).orElseThrow();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("ok");
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
