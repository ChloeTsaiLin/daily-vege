package com.tsailin.dailyvege.service.impl;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Long saveRestaurant(Restaurant restaurantRequest) {

        String googleId = restaurantRequest.getGooglePlaceId();

        if (restaurantRepository.existsByGooglePlaceId(googleId)) {
            throw new RuntimeException("GooglePlaceId(%s) already exists.".formatted(googleId));
        }
        restaurantRequest.setCreatedDate(OffsetDateTime.now());
        restaurantRequest.setLastModifiedDate(OffsetDateTime.now());
        Restaurant savedRestaurant = restaurantRepository.save(restaurantRequest);

        return savedRestaurant.getId();
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    @Override
    public Long updateRestaurant(Long restaurantId, Restaurant restaurantRequest) {

        return restaurantRepository.findById(restaurantId).map(existingRestaurant -> {

            existingRestaurant.setName(restaurantRequest.getName());
//             existingRestaurant.setGooglePlaceId(restaurantRequest.getGooglePlaceId());
             existingRestaurant.setLatitude(restaurantRequest.getLatitude());
             existingRestaurant.setLongitude(restaurantRequest.getLongitude());
             existingRestaurant.setVegType(restaurantRequest.getVegType());
             existingRestaurant.setLastModifiedDate(OffsetDateTime.now());

            return restaurantRepository.save(existingRestaurant).getId();

        }).orElseThrow(() -> new RuntimeException("restaurantId(%s) NOT FOUND.".formatted(restaurantId)));
    }

    @Override
    public void deleteRestaurant(Long restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

}
