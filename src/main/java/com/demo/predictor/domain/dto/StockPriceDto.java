package com.demo.predictor.domain.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record StockPriceDto(String stockId, Date timestamp, Double price) {
}
