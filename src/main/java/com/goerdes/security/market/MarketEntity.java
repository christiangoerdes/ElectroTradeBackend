package com.goerdes.security.market;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
