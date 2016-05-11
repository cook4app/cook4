package com.beppeben.cook4.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CurrencyUtils {

    public static List<String> symbols;
    public static Map<String, MyCurrency> codesmap;
    public static Map<String, MyCurrency> symbolsmap;

    public static void initialize() {
        codesmap = new HashMap<>();
        symbolsmap = new HashMap<>();
        List<MyCurrency> currencyList = new ArrayList<MyCurrency>();
        Locale[] locs = Locale.getAvailableLocales();
        for (Locale loc : locs) {
            try {
                Currency c = Currency.getInstance(loc);
                MyCurrency curr = new MyCurrency(c.getCurrencyCode(), c.getSymbol());
                if (!codesmap.containsKey(c.getCurrencyCode())) {
                    currencyList.add(curr);
                }
                codesmap.put(c.getCurrencyCode(), curr);
                symbolsmap.put(c.getSymbol(), curr);

            } catch (Exception exc) {
            }
        }
        Collections.sort(currencyList);

        symbols = new ArrayList<String>();
        //local first
        Currency localCurr = Currency.getInstance(Locale.getDefault());
        symbols.add(localCurr.getSymbol());
        for (int i = 0; i < currencyList.size(); i++) {
            MyCurrency c = currencyList.get(i);
            if (!symbols.contains(c.symbol)) {
                symbols.add(c.symbol);
            }
        }
        //TODO: move limits to values folder
        setLimits("INR", 0F, 5F, 100);
        setLimits("MYR", 0F, 1F, 100);
    }

    private static void setLimits(String code, Float minprice, Float pricestep, int choices) {
        if (codesmap.containsKey(code)) {
            MyCurrency curr = codesmap.get(code);
            curr.minprice = minprice;
            curr.pricestep = pricestep;
            curr.choices = choices;
        }
    }

    public static class MyCurrency implements Comparable<MyCurrency> {
        public String code;
        public String symbol;
        public Float minprice;
        public Float pricestep;
        public int choices;

        public MyCurrency(String code, String symbol) {
            this.code = code;
            this.symbol = symbol;
            //default values
            this.minprice = 0F;
            this.pricestep = 0.5F;
            this.choices = 400;
        }

        @Override
        public int compareTo(MyCurrency another) {
            if (symbol.toCharArray().length > another.symbol.toCharArray().length) return 1;
            else if (symbol.toCharArray().length == another.symbol.toCharArray().length) return 0;
            else return -1;
        }
    }

    public static String getSymbolFromCode(String code) {
        if (code == null) return symbols.get(0);
        if (codesmap.containsKey(code)) return codesmap.get(code).symbol;
        else return symbols.get(0);
    }

    public static String getCodeFromSymbol(String symbol) {
        String def = symbolsmap.get(symbols.get(0)).code;
        if (symbol == null) return def;
        if (symbolsmap.containsKey(symbol)) return symbolsmap.get(symbol).code;
        else return def;
    }
}
