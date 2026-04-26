package com.tsailin.dailyvege.repository;

import com.tsailin.dailyvege.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByGooglePlaceId(String googlePlaceId);
}
