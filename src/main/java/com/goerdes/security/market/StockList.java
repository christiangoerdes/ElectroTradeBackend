package com.goerdes.security.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockList {

    @JsonProperty("stocks")
    private List<MarketEntity> stocks;

    @JsonProperty("timestamps")
    private List<String> timestamps;

}
