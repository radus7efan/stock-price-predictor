package com.demo.predictor.service;

import com.demo.predictor.domain.dto.StockPriceDto;
import com.demo.predictor.domain.enums.StockExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    public List<StockPriceDto> getStockPrice(StockExchange stockExchange, Date timestamp) {

        return List.of(StockPriceDto.builder().build());
    }

    public List<StockPriceDto> predictPrices(Integer numberOfPredictions) {

        return List.of(StockPriceDto.builder().build());
    }
}
