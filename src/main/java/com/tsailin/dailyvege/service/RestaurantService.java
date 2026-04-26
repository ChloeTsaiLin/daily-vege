package com.tsailin.dailyvege.service;

import com.tsailin.dailyvege.entity.Restaurant;


public interface RestaurantService {

    Long saveRestaurant(Restaurant restaurant);

    Restaurant getRestaurantById(Long Id);

}

