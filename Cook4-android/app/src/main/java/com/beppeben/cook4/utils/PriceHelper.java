package com.beppeben.cook4.utils;

public interface PriceHelper {

    public void registerPrice(Float price, String type);

    public void registerCurrency(String currency, String type);

    public void unCheckOption(String type);
}
