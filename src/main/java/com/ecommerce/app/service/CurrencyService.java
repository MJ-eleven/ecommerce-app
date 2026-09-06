package com.ecommerce.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class CurrencyService {

    private static final double EUR_TO_XOF = 655.96;
    private static final double USD_TO_XOF = 590.0; // Taux approximatif

    @Value("${currency.default:XOF}")
    private String defaultCurrency;

    private String currentCurrency;

    @PostConstruct
    public void init() {
        this.currentCurrency = defaultCurrency;
        System.out.println("========================================");
        System.out.println("💰 DEVISE PAR DÉFAUT : " + currentCurrency);
        System.out.println("========================================");
    }

    public enum Currency {
        EUR("€", "Euro", "EUR"),
        USD("$", "Dollar", "USD"),
        XOF("CFA", "Franc CFA", "XOF");

        private final String symbol;
        private final String name;
        private final String code;

        Currency(String symbol, String name, String code) {
            this.symbol = symbol;
            this.name = name;
            this.code = code;
        }

        public String getSymbol() { return symbol; }
        public String getName() { return name; }
        public String getCode() { return code; }
    }

    public void setCurrency(String currency) {
        if (currency.equals("EUR") || currency.equals("USD") || currency.equals("XOF")) {
            this.currentCurrency = currency;
            System.out.println("💰 Devise changée : " + currentCurrency);
        }
    }

    public String getCurrentCurrency() {
        return currentCurrency;
    }

    public Currency getCurrencyInfo() {
        for (Currency c : Currency.values()) {
            if (c.getCode().equals(currentCurrency)) {
                return c;
            }
        }
        return Currency.XOF;
    }

    /**
     * 🔥 Convertit un prix depuis le FCFA vers la devise choisie
     * @param amountInXOF Montant en FCFA
     * @return Montant converti dans la devise actuelle
     */
    public BigDecimal convertFromXOF(BigDecimal amountInXOF) {
        if (amountInXOF == null) return BigDecimal.ZERO;

        switch (currentCurrency) {
            case "EUR":
                return amountInXOF.divide(BigDecimal.valueOf(EUR_TO_XOF), 2, RoundingMode.HALF_UP);
            case "USD":
                return amountInXOF.divide(BigDecimal.valueOf(USD_TO_XOF), 2, RoundingMode.HALF_UP);
            case "XOF":
            default:
                return amountInXOF.setScale(0, RoundingMode.HALF_UP);
        }
    }

    /**
     * 🔥 Formate un prix en FCFA vers la devise choisie
     * @param amountInXOF Montant en FCFA
     * @return Prix formaté avec symbole de la devise
     */
    public String formatPrice(BigDecimal amountInXOF) {
        if (amountInXOF == null) return getSymbol() + " 0";

        BigDecimal converted = convertFromXOF(amountInXOF);
        Currency currencyInfo = getCurrencyInfo();

        NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);

        if (currentCurrency.equals("XOF")) {
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
        } else {
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
        }

        return currencyInfo.getSymbol() + " " + formatter.format(converted);
    }

    public String formatPrice(double amountInXOF) {
        return formatPrice(BigDecimal.valueOf(amountInXOF));
    }

    public String getSymbol() {
        return getCurrencyInfo().getSymbol();
    }
}