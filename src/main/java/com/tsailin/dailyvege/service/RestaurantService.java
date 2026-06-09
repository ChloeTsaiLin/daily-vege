package com.tsailin.dailyvege.service;

import com.tsailin.dailyvege.dto.RestaurantRequestDto;
import com.tsailin.dailyvege.entity.Restaurant;
import jakarta.validation.Valid;


public interface RestaurantService {
    Long saveRestaurant(Restaurant restaurantRequest);

    Restaurant getRestaurantById(Long restaurantId);

    void updateRestaurant(Long restaurantId, @Valid RestaurantRequestDto restaurantRequestDto);

    void deleteRestaurant(Long restaurantId);

    Boolean existsById(Long restaurantId);
}

