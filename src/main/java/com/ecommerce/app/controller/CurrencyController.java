package com.ecommerce.app.controller;

import com.ecommerce.app.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/switch")
    public String switchCurrency(@RequestParam String currency, @RequestParam String redirect) {
        currencyService.setCurrency(currency);
        return "redirect:" + redirect;
    }
}