package com.demo.predictor.controller.advice;

import com.demo.predictor.domain.enums.StockExchange;
import com.demo.predictor.exception.StockPricePredictorError;
import com.demo.predictor.exception.StockPricePredictorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;

/**
 * Exception handler for different type of exceptions that can be thrown by the application.
 * Handles and logs multiple types and maps them to a common error model so that all the exceptions are in the same format.
 */
@Slf4j
@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    private static final String ERROR_MESSAGE = "An ERROR ocurred: ";


    /**
     * Exception handler for {@link StockPricePredictorException} thrown by the application.
     */
    @ExceptionHandler(value = {StockPricePredictorException.class})
    protected ResponseEntity<StockPricePredictorError> handleStoreManagementToolException(
            StockPricePredictorException exception
    ) {

        log.error(ERROR_MESSAGE, exception);

        var error = new StockPricePredictorError(
                exception.getStatus().name(),
                exception.getStatus().value(),
                exception.getMessage(),
                OffsetDateTime.now()
        );

        return new ResponseEntity<>(error, exception.getStatus());
    }

    /**
     * Handler for {@link MethodArgumentTypeMismatchException} exceptions, thrown when the API is called with an
     * unsupported value or a wrong format value.
     * E.g A value that is not in {@link StockExchange} or a wrong date format.
     */
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<StockPricePredictorError> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {

        log.error(ERROR_MESSAGE, exception);

        var unsupportedValueMessage =
                "`%s` is not a supported value or has the wrong format.Please try again using a different value.";
        var supportedValues = StringUtils.join(StockExchange.values(), ", ");

        var error = new StockPricePredictorError(
                HttpStatus.BAD_REQUEST.name(),
                HttpStatus.BAD_REQUEST.value(),
                unsupportedValueMessage.formatted(exception.getValue(), supportedValues),
                OffsetDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * General handler for other types of exceptions thrown by the application.
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<StockPricePredictorError> handleGeneralException(Exception exception) {

        log.error(ERROR_MESSAGE, exception);

        var error = new StockPricePredictorError(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage(),
                OffsetDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
