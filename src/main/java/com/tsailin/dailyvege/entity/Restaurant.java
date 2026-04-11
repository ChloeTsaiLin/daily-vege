package com.tsailin.dailyvege.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "google_place_id",unique = true)
    private String googlePlaceId;

    private String vegType;
    //TODO enum：全素vegan/五辛素gokun/奶蛋素lactoOvo

    private Timestamp createdDate;

    private Timestamp lastModifiedDate;
}
