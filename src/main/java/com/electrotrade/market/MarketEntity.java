package com.electrotrade.market;

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
        double newValue = lastValue * (1 + percentageChange);
        double randomInt = (random.nextInt(9) - 3)* 0.1 * newValue;
        newValue += randomInt;
        priceHistory.add(Math.round(newValue * 100.0) / 100.0);
    }
}
