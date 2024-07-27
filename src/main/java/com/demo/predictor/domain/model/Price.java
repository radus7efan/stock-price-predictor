package com.demo.predictor.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "price")
@Table(name = "price")
public class Price {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDate timestamp;

    private Double priceValue;

}
