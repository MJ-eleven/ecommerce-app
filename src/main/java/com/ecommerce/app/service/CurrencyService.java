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

    private static final double EUR_TO_USD = 1.08;
    private static final double EUR_TO_XOF = 655.96;

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
     * Convertit un montant en euros vers la devise actuelle
     * @param amountInEuro Montant en euros
     * @return Montant converti dans la devise actuelle
     */
    public BigDecimal convert(BigDecimal amountInEuro) {
        if (amountInEuro == null) return BigDecimal.ZERO;

        switch (currentCurrency) {
            case "USD":
                return amountInEuro.multiply(BigDecimal.valueOf(EUR_TO_USD))
                        .setScale(2, RoundingMode.HALF_UP);
            case "XOF":
                // 🔥 Conversion avec 2 décimales pour éviter les pertes
                BigDecimal result = amountInEuro.multiply(BigDecimal.valueOf(EUR_TO_XOF))
                        .setScale(2, RoundingMode.HALF_UP);
                // 🔥 Arrondi à l'entier pour l'affichage
                return result.setScale(0, RoundingMode.HALF_UP);
            default:
                return amountInEuro.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Formate un prix en euros vers la devise actuelle
     * @param amountInEuro Montant en euros
     * @return Prix formaté avec symbole de la devise
     */
    public String formatPrice(BigDecimal amountInEuro) {
        if (amountInEuro == null) return getSymbol() + " 0";

        BigDecimal converted = convert(amountInEuro);
        Currency currencyInfo = getCurrencyInfo();

        NumberFormat formatter;
        if (currentCurrency.equals("XOF")) {
            // 🔥 FCFA : pas de décimales
            formatter = NumberFormat.getInstance(Locale.FRENCH);
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
        } else {
            // 🔥 EUR/USD : 2 décimales
            formatter = NumberFormat.getInstance(Locale.FRENCH);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
        }

        return currencyInfo.getSymbol() + " " + formatter.format(converted);
    }

    /**
     * Formate un prix en euros vers la devise actuelle (version double)
     * @param amountInEuro Montant en euros
     * @return Prix formaté avec symbole de la devise
     */
    public String formatPrice(double amountInEuro) {
        return formatPrice(BigDecimal.valueOf(amountInEuro));
    }

    /**
     * Retourne le symbole de la devise actuelle
     * @return Symbole de la devise
     */
    public String getSymbol() {
        return getCurrencyInfo().getSymbol();
    }

    /**
     * Convertit un prix saisi en FCFA vers les euros pour le stockage
     * @param amountInXOF Montant en FCFA
     * @return Montant converti en euros
     */
    public BigDecimal convertXOFToEuro(BigDecimal amountInXOF) {
        if (amountInXOF == null) return BigDecimal.ZERO;
        return amountInXOF.divide(BigDecimal.valueOf(EUR_TO_XOF), 4, RoundingMode.HALF_UP);
    }
}