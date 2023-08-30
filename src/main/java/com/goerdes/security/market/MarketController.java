package com.goerdes.security.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "http://localhost:5173")
@Controller
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {
}
