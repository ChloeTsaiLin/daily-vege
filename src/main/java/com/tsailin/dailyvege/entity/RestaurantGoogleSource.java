package com.tsailin.dailyvege.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "restaurant_google_source")
@Data
public class RestaurantGoogleSource {
    @Id
    @Column(name = "restaurant_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "google_place_id",unique = true, nullable = false)
    @JsonProperty("id")
    private String googlePlaceId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "formatted_address", length = 500)
    private String formattedAddress;

    private Double latitude;
    private Double longitude;

    private Double rating;

    @Column(name = "raw_types", columnDefinition = "TEXT")
    private String rawTypes;

    private String priceLevel;

    private Boolean servesVegetarianFood;

    @Column(name = "weekday_descriptions", columnDefinition = "TEXT")
    private String weekdayDescriptions;

    private OffsetDateTime createdDate;

    private OffsetDateTime lastModifiedDate;
}
