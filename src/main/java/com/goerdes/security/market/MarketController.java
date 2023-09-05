package com.goerdes.security.market;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
