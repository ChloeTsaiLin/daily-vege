package com.tsailin.dailyvege.service.impl;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Long saveRestaurant(Restaurant restaurant) {

        String googleId = restaurant.getGooglePlaceId();

        if (restaurantRepository.existsByGooglePlaceId(googleId)) {
            throw new RuntimeException("GooglePlaceId(%s) already exists.".formatted(googleId));
        } 
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return savedRestaurant.getId();
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }
}
