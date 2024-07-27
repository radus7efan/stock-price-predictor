package com.demo.predictor.domain.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StockPriceDto(String stockId, LocalDate timestamp, Double price) {
}
