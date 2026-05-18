package com.tsailin.dailyvege.service;

import com.tsailin.dailyvege.entity.Restaurant;
import jakarta.validation.Valid;


public interface RestaurantService {
    Long saveRestaurant(Restaurant restaurantRequest);

    Restaurant getRestaurantById(Long restaurantId);

    void updateRestaurant(Long restaurantId, @Valid Restaurant restaurantRequest);

    void deleteRestaurant(Long restaurantId);

    Boolean existsById(Long restaurantId);
}

