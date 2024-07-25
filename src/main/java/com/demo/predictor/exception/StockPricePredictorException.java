package com.demo.predictor.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StockPricePredictorException extends RuntimeException {

    private final HttpStatus status;

    public StockPricePredictorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public StockPricePredictorException(HttpStatus status, String message, Object... arguments) {
        super(String.format(message, arguments));
        this.status = status;
    }
}
