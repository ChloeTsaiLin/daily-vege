package com.tsailin.dailyvege.service.impl;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        if (restaurantRequest.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid Request: ID should not be provided for creation.");
        }
        restaurantRequest.setVegType(restaurantRequest.getVegType());
        restaurantRequest.setRestaurantStyle(restaurantRequest.getRestaurantStyle());
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
    public void updateRestaurant(Long restaurantId, Restaurant restaurantRequest) {

        Restaurant existingRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        existingRestaurant.setVegType(restaurantRequest.getVegType());
        existingRestaurant.setRestaurantStyle(restaurantRequest.getRestaurantStyle());
        existingRestaurant.setLastModifiedDate(OffsetDateTime.now());

        restaurantRepository.save(existingRestaurant);

    }

    @Override
    public void deleteRestaurant(Long restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

    @Override
    public Boolean existsById(Long restaurantId) {
        return restaurantRepository.existsById(restaurantId);
    }

}
