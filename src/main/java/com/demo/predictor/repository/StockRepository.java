package com.demo.predictor.repository;

import com.demo.predictor.domain.model.Stock;
import com.demo.predictor.exception.StockPricePredictorException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends CrudRepository<Stock, Long> {

    Optional<Stock> findByName(String name);

    default Stock findByNameRequired(String name) {
        return findByName(name)
                .orElseThrow(() -> new StockPricePredictorException(HttpStatus.NOT_FOUND,
                        "Could not find the Stock with name: %s!", name));
    }
}
