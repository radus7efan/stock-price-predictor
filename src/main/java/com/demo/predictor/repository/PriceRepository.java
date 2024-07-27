package com.demo.predictor.repository;

import com.demo.predictor.domain.model.Price;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {

    @Query(value = """
            SELECT timestamp FROM price ORDER BY timestamp LIMIT 1;
            """, nativeQuery = true)
    LocalDate findFirstTimestamp();

    @Query(value = """
            SELECT timestamp FROM price ORDER BY timestamp DESC LIMIT 1;
            """, nativeQuery = true)
    LocalDate findLastTimestamp();
}
