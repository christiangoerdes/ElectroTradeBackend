package com.goerdes.security.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    @JsonProperty("balance")
    private Double balance;

    @JsonProperty("quantity")
    private Integer quantity;
}
