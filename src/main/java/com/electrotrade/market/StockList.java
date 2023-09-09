package com.electrotrade.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockList {

    @JsonProperty("stocks")
    private List<MarketEntityResponse> stocks;

    @JsonProperty("timestamps")
    private List<String> timestamps;

}
