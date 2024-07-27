package com.demo.predictor.domain.model;

import java.time.LocalDate;

public interface StockPrice {

    String getName();

    String getExchangeName();

    Double getPriceValue();

    LocalDate getTimestamp();
}
