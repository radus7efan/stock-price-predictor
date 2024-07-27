package com.demo.predictor.eventlistener;

import com.demo.predictor.StockPricePredictorApplication;
import com.demo.predictor.domain.enums.StockExchange;
import com.demo.predictor.domain.model.Price;
import com.demo.predictor.domain.model.Stock;
import com.demo.predictor.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartUpEventListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Integer DEFAULT_NUMBER_OF_SAMPLES = 2;

    private static final String DEFAULT_VALUE_MESSAGE = " the default value of 2 files for each Stock Exchange will be used";

    private static final String INPUT_PATH = "/input/%s";

    private final ApplicationArguments applicationArguments;

    private final StockRepository stockRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // Get number of sample files from arguments or default value
        var numberOfFiles = getNumberOfSampleFiles(applicationArguments.getSourceArgs());
        log.info("Number of files to be sampled for each Stock Exchange: {}", numberOfFiles);

        for (StockExchange stockExchange : StockExchange.values()) {
            var filenames = getInputFilesForExchange(stockExchange, numberOfFiles);
            if (CollectionUtils.isEmpty(filenames)) {
                log.info("There are no input files for Exchange: {}. Moving on..", stockExchange);
            }
            filenames.forEach(filename -> saveStockPrices(stockExchange, filename));
        }

    }

    /**
     * Get the number of sample files to be used, or the default value if the number is not specified or has a wrong format.
     */
    private Integer getNumberOfSampleFiles(String[] args) {

        // If the number of files to be sampled is not provided, use the default value
        if (args.length == 0) {
            log.info("The number of files to be sampled was not provided;" + DEFAULT_VALUE_MESSAGE);
            return DEFAULT_NUMBER_OF_SAMPLES;
        } else {
            // If the argument is not a number, log and error and use the default value
            try {
                return Integer.parseInt(args[0]);
            } catch (Exception e) {
                log.error("The argument provided {} is not a number;" + DEFAULT_VALUE_MESSAGE, args[0]);
            }
        }

        return DEFAULT_NUMBER_OF_SAMPLES;
    }

    /**
     * Get a list of file names containing at most maxSize elements for the specified exchange.
     *
     * @param exchange value of {@link StockExchange}. E.g. LSE
     * @param maxSize  max number of files to be returned
     * @return List of file names for the specified exchange, no longer than maxSize.
     */
    private List<String> getInputFilesForExchange(StockExchange exchange, Integer maxSize) {
        var exchangeFolderURL = StockPricePredictorApplication.class.getResource(INPUT_PATH.formatted(exchange));

        if (exchangeFolderURL == null) {
            return List.of();
        }

        return Optional.ofNullable(new File(exchangeFolderURL.getPath()).listFiles())
                .map(Arrays::stream)
                .map(stream -> stream.map(File::getPath).limit(maxSize).toList())
                .orElse(List.of());
    }

    private void saveStockPrices(StockExchange stockExchange, String filePath) {

        try {
            var pricesInput = Files.readAllLines(Paths.get(filePath));

            if (CollectionUtils.isEmpty(pricesInput)) {
                log.error("File: {} is empty. Moving on to the next file.", extractFilenameFromPath(filePath));
                return;
            }

            var stockName = pricesInput.get(0).split(",")[0];
            var stock = new Stock();
            stock.setName(stockName);
            stock.setExchangeName(stockExchange.name());

            var prices = pricesInput.stream()
                    .map(line -> line.split(","))
                    .map(this::mapPrice)
                    .toList();

            stock.setPrices(prices);
            stockRepository.save(stock);

        } catch (IOException | ArrayIndexOutOfBoundsException exception) {
            log.error("File: {} could not be processed. Moving on to the next file.", extractFilenameFromPath(filePath));
        }
    }

    private Price mapPrice(String[] priceInput) {
        var price = new Price();

        price.setTimestamp(LocalDate.parse(priceInput[1], DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        price.setPriceValue(Double.valueOf(priceInput[2]));

        return price;
    }

    private String extractFilenameFromPath(String filePath) {
        var splitPath = filePath.split("[\\\\\\/]");
        return String.join("\\", List.of(splitPath[splitPath.length - 2], splitPath[splitPath.length - 1]));
    }

}
