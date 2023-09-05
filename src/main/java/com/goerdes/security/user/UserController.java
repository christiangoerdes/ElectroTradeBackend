package com.goerdes.security.user;

import com.goerdes.security.market.MarketService;
import com.goerdes.security.market.TransactionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MarketService marketService;

    @GetMapping("/data")
    public ResponseEntity<DemoData> getDemoData() {
        return ResponseEntity.ok(DemoData.builder().data("Demo").build());
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

    @GetMapping("/info/{marketId}")
    public ResponseEntity<Integer> getStockNumbersForId(HttpServletRequest request, @PathVariable("marketId") Integer marketId) throws AuthenticationException {
        return marketService.getStockNumbersForId(request, marketId);
    }
}
