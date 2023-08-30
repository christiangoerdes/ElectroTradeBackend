package com.goerdes.security.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return StockList.builder().stocks(marketRepo.findAll()).build();
    }
}
