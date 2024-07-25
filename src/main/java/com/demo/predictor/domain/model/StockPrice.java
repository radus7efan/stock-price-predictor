package com.demo.predictor.domain.model;

import lombok.Builder;

import java.util.Date;

@Builder
public record StockPrice(String stockId, Date timestamp, Double price) {
}
