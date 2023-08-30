package com.goerdes.security.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepo marketRepo;

    public void createMarketEntity(String name, List<Double> priceHistory) {
        marketRepo.save(MarketEntity.builder().name(name).priceHistory(priceHistory).build());
    }

    public StockList getAllStocks() {
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

        return formattedDates;
    }
}
