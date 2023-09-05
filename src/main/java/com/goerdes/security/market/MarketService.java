package com.goerdes.security.market;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final UserRepo userRepo;
    private final MarketRepo marketRepo;
    private final JwtService jwtService;

    ObjectMapper objectMapper = new ObjectMapper();

    public void createMarketEntity(String name, List<Double> priceHistory) {
        marketRepo.save(MarketEntity.builder().name(name).priceHistory(priceHistory).build());
    }

    public StockList getAllStocks(HttpServletRequest request) throws AuthenticationException {
        updateAllEntities();
        UserEntity user = extractUser(request);

        List<MarketEntity> stockEntities = marketRepo.findAll();
        Map<MarketEntity, Integer> ownedStocks = user.getStockQuantityMap();
        List<MarketEntityResponse> stocks = stockEntities.stream()
                .map(marketEntity -> {
                    MarketEntityResponse response = objectMapper.convertValue(marketEntity, MarketEntityResponse.class);
                    response.setQuantity(ownedStocks.getOrDefault(marketEntity, 0));
                    return response;
                })
                .collect(Collectors.toList());

        return StockList.builder().stocks(stocks).timestamps(getLast30DaysFormatted()).build();
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

    public ResponseEntity<TransactionResponse> buy(HttpServletRequest request, Integer marketId, int quantity) throws AuthenticationException {
        UserEntity user = extractUser(request);
        MarketEntity stock;
        try {
            stock = marketRepo.findById(marketId).orElseThrow();
            user.buyStock(stock, quantity);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userRepo.save(user);
        return new ResponseEntity<>(TransactionResponse.builder().balance(user.getBalance()).quantity(Optional.ofNullable(user.getStockQuantityMap().get(stock)).orElse(0)).build(), HttpStatus.OK);
    }

    public ResponseEntity<TransactionResponse> sell(HttpServletRequest request, Integer marketId, int quantity) throws AuthenticationException {
        UserEntity user = extractUser(request);
        MarketEntity stock;
        try {
            stock = marketRepo.findById(marketId).orElseThrow();
            user.sellStock(stock, quantity);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userRepo.save(user);
        return new ResponseEntity<>(TransactionResponse.builder().balance(user.getBalance()).quantity(Optional.ofNullable(user.getStockQuantityMap().get(stock)).orElse(0)).build(), HttpStatus.OK);
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

    public ResponseEntity<Integer> getStockNumbersForId(HttpServletRequest request, Integer marketId) throws AuthenticationException {
        UserEntity user = extractUser(request);
        MarketEntity stock;
        try {
            stock = marketRepo.findById(marketId).orElseThrow();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Integer quantity = Optional.ofNullable(user.getStockQuantityMap().get(stock)).orElse(0);
        return new ResponseEntity<>(quantity, HttpStatus.OK);

    }
}
