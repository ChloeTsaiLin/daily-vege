package com.tsailin.dailyvege.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "restaurants")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "veg_type")
    @JsonProperty("veg_type")
    private String vegType;
    //TODO enum：全素vegan/五辛素gokun/奶蛋素lactoOvo

    private String restaurantStyle;
    //TODO enum

    private OffsetDateTime createdDate;

    private OffsetDateTime lastModifiedDate;
}
