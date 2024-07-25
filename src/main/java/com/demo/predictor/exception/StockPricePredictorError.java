package com.demo.predictor.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StockPricePredictorError {

    private String title;

    private Integer status;

    private String details;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime timestamp;

    @Override
    public String toString() {
        return "class StockPricePredictorError {\n" +
                "\ttitle: " + title + "\n" +
                "\tstatus: " + status + "\n" +
                "\tdetails: " + details + "\n" +
                "\ttimestamp: " + timestamp + "\n" +
                "}";
    }
}
