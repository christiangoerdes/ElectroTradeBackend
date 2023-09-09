package com.electrotrade.market;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@CrossOrigin(origins = "http://localhost:5173")
@Controller
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/data")
    public ResponseEntity<StockList>getAllStocks(HttpServletRequest request) throws AuthenticationException {
        return new ResponseEntity<>(marketService.getAllStocks(request), HttpStatus.OK);
    }

    @PostMapping("/buy/{marketId}")
    public ResponseEntity<TransactionResponse> buyStock(
            HttpServletRequest request,
            @PathVariable("marketId") Integer marketId,
            @RequestParam("quantity") int quantity) throws AuthenticationException {
        return marketService.buy(request, marketId, quantity);
    }

    @PostMapping("/sell/{marketId}")
    public ResponseEntity<TransactionResponse> sellStock(
            HttpServletRequest request,
            @PathVariable("marketId") Integer marketId,
            @RequestParam("quantity") int quantity) throws AuthenticationException {
        return marketService.sell(request, marketId, quantity);
    }
}
