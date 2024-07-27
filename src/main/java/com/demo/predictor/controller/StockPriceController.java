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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, List<StockPriceDto>>> getStockPrices(
            @Validated(StockExchange.class) @RequestParam(required = false) StockExchange exchange,
            @RequestParam(required = false) String stockName,
            @RequestParam(required = false) String timestamp
    ) {
        return new ResponseEntity<>(
                stockPriceService.getStockPrices(exchange, stockName, timestamp),
                HttpStatus.OK
        );
    }

    @PostMapping("/predict")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPriceDto.class))
    })
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StockPricePredictorError.class))
    })
    public ResponseEntity<List<StockPriceDto>> getPredictedPrices(@RequestBody List<StockPriceDto> stockPrices) {
        return new ResponseEntity<>(
                stockPriceService.predictPrices(stockPrices),
                HttpStatus.OK
        );
    }
}
