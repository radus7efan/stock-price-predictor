package com.demo.predictor.controller;

import com.demo.predictor.domain.dto.StockPriceDto;
import com.demo.predictor.domain.enums.StockExchange;
import com.demo.predictor.exception.StockPricePredictorError;
import com.demo.predictor.service.StockPriceService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stock-price")
public class StockPriceController {

    private final StockPriceService stockPriceService;

    @GetMapping("/prices")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPriceDto.class))
    })
    @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPricePredictorError.class))
    })
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPricePredictorError.class))
    })
    public ResponseEntity<List<StockPriceDto>> getStockPrice(@Validated(StockExchange.class)
                                                             @RequestParam StockExchange exchange,
                                                             @RequestParam(required = false) Date timestamp
    ) {
        return new ResponseEntity<>(
                stockPriceService.getStockPrice(exchange, timestamp),
                HttpStatus.OK
        );
    }

    @GetMapping("/predict")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPriceDto.class))
    })
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPricePredictorError.class))
    })
    public ResponseEntity<List<StockPriceDto>> getPredictedPrices(@RequestParam(defaultValue = "3", required = false)
                                                                  Integer numberOfPredictions
    ) {
        return new ResponseEntity<>(
                stockPriceService.predictPrices(numberOfPredictions),
                HttpStatus.OK
        );
    }
}
