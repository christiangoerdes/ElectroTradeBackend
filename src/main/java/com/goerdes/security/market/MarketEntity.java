package com.goerdes.security.market;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Random;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "market")
public class MarketEntity {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @ElementCollection
    private List<Double> priceHistory;

    public void updatePriceHistory() {
        priceHistory.remove(0);
        Random random = new Random();
        Double lastValue = priceHistory.get(priceHistory.size() -1 );
        double percentageChange = (random.nextDouble() - 0.5) * 0.10;
        Double newValue = lastValue * (1 + percentageChange);
        priceHistory.add(newValue);
    }
}
