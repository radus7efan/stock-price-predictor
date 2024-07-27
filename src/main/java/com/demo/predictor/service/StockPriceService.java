package com.demo.predictor.service;

import com.demo.predictor.domain.dto.StockPriceDto;
import com.demo.predictor.domain.enums.StockExchange;
import com.demo.predictor.domain.model.StockPrice;
import com.demo.predictor.exception.StockPricePredictorException;
import com.demo.predictor.repository.PriceRepository;
import com.demo.predictor.repository.StockPriceRepository;
import com.demo.predictor.repository.StockRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private static final String OUTPUT_PATH = "/output/";

    private final StockRepository stockRepository;

    private final PriceRepository priceRepository;

    private final StockPriceRepository stockPriceRepository;

    public Map<String, List<StockPriceDto>> getStockPrices(StockExchange exchange, String stockName, String timestamp) {
        log.info("Calling method -- getStockPrices -- with exchange: {}, stockName: {} and timestamp: {}",
                exchange, stockName, timestamp);

        validateGetStockPricesInput(stockName);

        // If timestamp is not provided, generate a random timestamp
        var date = StringUtils.isEmpty(timestamp) ?
                generateRandomTimestamp() :
                LocalDate.parse(timestamp, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        if (exchange == null) {
            // Retrieve prices for all the exchanges and all the stocks
            if (StringUtils.isEmpty(stockName)) {
                Map<String, List<StockPriceDto>> stockPricesMap = new HashMap<>();

                for (StockExchange stockExchange : StockExchange.values()) {
                    var stockPrices = stockPriceRepository
                            .findAllByExchangeNameAndTimestampAfter(stockExchange.name(), date);
                    stockPricesMap.putAll(mapStockPrices(stockPrices));
                }

                return stockPricesMap;
            }

            // Retrieve prices for a specific stock name
            var stockPrices = stockPriceRepository.findAllStockNameAndTimestampAfter(stockName, date);
            return mapStockPrices(stockPrices);
        }

        // Retrieve prices for all the stocks of a specific exchange
        if (StringUtils.isEmpty(stockName)) {
            var stockPrices = stockPriceRepository.findAllByExchangeNameAndTimestampAfter(exchange.name(), date);
            return mapStockPrices(stockPrices);
        }

        // Retrieve prices for a specific exchange and stock
        var stockPrices =
                stockPriceRepository.findAllByExchangeNameAndStockNameAndTimestampAfter(exchange.name(), stockName, date);
        return mapStockPrices(stockPrices);
    }

    public List<StockPriceDto> predictPrices(List<StockPriceDto> stockPrices) {
        log.info("Calling method -- predictPrices -- with stockPrices: {} with size: {}", stockPrices, stockPrices.size());

        validatePredictPricesInput(stockPrices);

        var sortedPrices = stockPrices.stream().sorted(Comparator.comparing(StockPriceDto::price).reversed()).toList();
        var predictedPrices = predictFuturePrices(stockPrices.get(stockPrices.size() - 1), sortedPrices.get(1));

        stockPrices.addAll(predictedPrices);

        writeOutputFile(stockPrices);

        return stockPrices;
    }

    /**
     * Fetches the first and last timestamps on which there are prices persisted in the database and generates a random
     * timestamp between the two of them ( the upper limit should be at least 10 days before the last timestamp, so we
     * are sure that we retrieve at least 10 prices ).
     */
    private LocalDate generateRandomTimestamp() {
        var fromTimestamp = priceRepository.findFirstTimestamp();
        var toTimestamp = priceRepository.findLastTimestamp();
        var daysBetween = (int) ChronoUnit.DAYS.between(fromTimestamp, toTimestamp);

        return fromTimestamp.plusDays(ThreadLocalRandom.current().nextInt(0, daysBetween - 10));
    }

    private Map<String, List<StockPriceDto>> mapStockPrices(List<StockPrice> stockPrices) {
        return stockPrices.stream()
                .collect(
                        Collectors.groupingBy(StockPrice::getExchangeName, HashMap::new,
                                Collectors.mapping(
                                        stockPrice ->
                                                new StockPriceDto(
                                                        stockPrice.getName(),
                                                        stockPrice.getTimestamp(),
                                                        stockPrice.getPriceValue()
                                                ),
                                        Collectors.toList())
                        )
                );
    }

    /**
     * Predict prices simple algorithm.
     * Returns a list of 3 prices:
     * - first element is identical to the secondHighPrice which is the second-highest price in the input dataset
     * - second element has the price = first_price +|- |first_price - second_highest_price| / 2
     * - third element has the price = second_price +|- |second_price - first_price| / 4
     * Timestamps for each price are consecutive days starting from the last price value in the input dataset.
     * </p>
     * For the second and third prices, the operation (add or subtract of the difference) is randomly decided.
     */
    private List<StockPriceDto> predictFuturePrices(StockPriceDto priceN, StockPriceDto secondHighPrice) {
        var priceDif = Math.abs(priceN.price() - secondHighPrice.price()) / 2;
        var secondPrice = computePrice(secondHighPrice.price(), priceDif);

        priceDif = Math.abs(secondHighPrice.price() - secondPrice) / 4;

        return List.of(
                new StockPriceDto(priceN.stockId(), priceN.timestamp().plusDays(1), priceN.price()),
                new StockPriceDto(priceN.stockId(), priceN.timestamp().plusDays(2), secondPrice),
                new StockPriceDto(priceN.stockId(), priceN.timestamp().plusDays(3), computePrice(secondPrice, priceDif))
        );
    }

    private Double computePrice(Double price, Double priceDif) {
        var computed = ThreadLocalRandom.current().nextBoolean() ?
                price + priceDif :
                price - priceDif;

        // Format it to 2 decimals for the sake of pretty printing, but in general we should keep the exact value
        // especially when we work with prices
        return Math.floor(computed * 100) / 100;
    }

    /**
     * Write stock price prediction output in a csv file.
     * The file structure will be the same as for the input files: /output/<exchange>/<stock>
     * Each file name will have the date and time on which it was created concatenated,
     * so the output won't be overwritten, but a new file is generated each time.
     */
    private void writeOutputFile(List<StockPriceDto> prices) {
        var stockName = prices.get(0).stockId();
        var exchange = stockRepository.findByNameRequired(stockName).getExchangeName();
        var date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyy_hh_mm_ss"));

        File csvOutputFile = createFile(exchange, stockName + "-" + date + ".csv");

        if (csvOutputFile == null) {
            return;
        }
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {

            prices.stream()
                    .map(price -> String.join(",",
                                    price.stockId(),
                                    price.timestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                    price.price().toString()
                            )
                    )
                    .forEach(pw::println);

        } catch (FileNotFoundException exception) {
            log.error("Could not find file: {}. The output was not saved.", exchange + "/" + stockName + "-" + date + ".csv");
        }
    }

    private File createFile(String exchange, String filePath) {

        try {
            var outputPath = StockPriceService.class.getResource(OUTPUT_PATH + exchange);
            if (outputPath == null) {
                boolean isDirCreated =
                        new File(StockPriceService.class.getResource("/").getPath() + OUTPUT_PATH + exchange).mkdirs();

                if (!isDirCreated) {
                    log.error("Could not create directory for exchange: {}. The output was not saved.", exchange);
                    return null;
                }
                outputPath = StockPriceService.class.getResource(OUTPUT_PATH + exchange);
            }
            File csvOutputFile = new File(outputPath.getPath() + "/" + filePath);

            boolean fileCreated = csvOutputFile.createNewFile();
            if (!fileCreated) {
                log.error("Could not create file: {}. The output was not saved.", filePath);
                return null;
            }
            return csvOutputFile;
        } catch (NullPointerException | IOException e) {
            log.error("Could not create file: {}. The output was not saved.", filePath);
        }

        return null;
    }

    private void validateGetStockPricesInput(String stockName) {
        if (StringUtils.isNotEmpty(stockName)) {
            stockRepository.findByNameRequired(stockName);
        }
    }

    private void validatePredictPricesInput(List<StockPriceDto> input) {
        if (input.size() < 10) {
            var errorMessage = "Input stock prices should be at least 10 for a good prediction. Input size: %s"
                    .formatted(input.size());
            log.error(errorMessage);
            throw new StockPricePredictorException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}
