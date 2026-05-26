package com.tsailin.dailyvege.service;


import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.entity.RestaurantGoogleSource;

import java.util.Map;

public interface GooglePlaceService {

    Restaurant importGoogleRestaurant(String placeId);

    Restaurant syncGoogleRestaurant(Long id);

    void mapGoogleDataToEntity(Map<String, Object> googleData, RestaurantGoogleSource source);

}
