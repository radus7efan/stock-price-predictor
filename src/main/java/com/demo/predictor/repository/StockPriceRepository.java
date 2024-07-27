package com.demo.predictor.repository;

import com.demo.predictor.domain.model.Stock;
import com.demo.predictor.domain.model.StockPrice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockPriceRepository extends CrudRepository<Stock, Long> {

    /**
     * Find first 10 stock prices for each stock for a specific exchange starting from a given timestamp.
     */
    @Query(value = """
             SELECT exchange_name,
                    name,
                    price_value,
                    timestamp
             FROM   (SELECT Row_number()
                              OVER(
                                partition BY stock_prices.name
                                ORDER BY stock_prices.name) AS rn,
                            stock_prices.exchange_name,
                            stock_prices.name,
                            stock_prices.price_value,
                            stock_prices.timestamp
                     FROM   (SELECT exchange_name,
                                    prices.name,
                                    price_value,
                                    timestamp
                             FROM   (SELECT stock.name
                                     FROM   stock
                                     WHERE  stock.exchange_name = ?1) names
                                    JOIN (SELECT s.name,
                                                 s.exchange_name,
                                                 p.price_value,
                                                 p.timestamp
                                          FROM   stock s,
                                                 price p
                                          WHERE  s.id = p.stock_id AND p.timestamp >= ?2) prices
                                      ON names.name = prices.name) AS stock_prices)
             WHERE  rn <= 10;
            """,
            nativeQuery = true)
    List<StockPrice> findAllByExchangeNameAndTimestampAfter(String exchangeName, LocalDate timestamp);

    /**
     * Find first 10 stock prices for a specific exchange & stock name starting from a given timestamp.
     */
    @Query(value = """
             SELECT s.name,
                    s.exchange_name,
                    p.price_value,
                    p.timestamp
             FROM   stock s,
                    price p
             WHERE s.name = ?2 AND s.id = p.stock_id AND s.exchange_name = ?1 AND p.timestamp >= ?3
             LIMIT 10;
            """,
            nativeQuery = true)
    List<StockPrice> findAllByExchangeNameAndStockNameAndTimestampAfter(String exchangeName, String stockName, LocalDate timestamp);


    /**
     * Find first 10 stock prices for a specific stock name starting from a given timestamp.
     */
    @Query(value = """
             SELECT new s.name,
                    s.exchange_name,
                    p.price_value,
                    p.timestamp
             FROM   stock s,
                    price p
             WHERE s.name = ?2 AND s.id = p.stock_id AND p.timestamp >= ?3
             LIMIT 10;
            """,
            nativeQuery = true)
    List<StockPrice> findAllStockNameAndTimestampAfter(String stockName, LocalDate timestamp);

}
