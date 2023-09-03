package com.goerdes.security.user;

import com.goerdes.security.market.MarketService;
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
    public ResponseEntity<String> buyMarket(
            HttpServletRequest request,
            @PathVariable("marketId") Integer marketId,
            @RequestParam("quantity") int quantity) throws AuthenticationException {
        return marketService.buy(request, marketId, quantity);
    }
}
