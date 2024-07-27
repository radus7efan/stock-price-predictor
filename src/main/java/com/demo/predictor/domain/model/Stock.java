package com.demo.predictor.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name = "stock")
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String exchangeName;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_id")
    List<Price> prices;
}
