package com.tsailin.dailyvege.repository;

import com.tsailin.dailyvege.entity.RestaurantGoogleSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleSourceRepository extends JpaRepository<RestaurantGoogleSource, String> {
    Optional<RestaurantGoogleSource> findByGooglePlaceId(String placeId);
    Optional<RestaurantGoogleSource> findById(Long id);
}
