package com.tsailin.dailyvege.service.impl;

import com.tsailin.dailyvege.dto.RestaurantRequestDto;
import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public Long saveRestaurant(RestaurantRequestDto restaurantRequestDto) {

        Restaurant restaurant = new Restaurant();
        restaurant.setVegType(restaurantRequestDto.getVegType());
        restaurant.setRestaurantStyle(restaurantRequestDto.getRestaurantStyle());
        restaurant.setCreatedDate(OffsetDateTime.now());
        restaurant.setLastModifiedDate(OffsetDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant.getId();
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    @Override
    public void updateRestaurant(Long restaurantId, RestaurantRequestDto restaurantRequestDto) {

        Restaurant existingRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        existingRestaurant.setVegType(restaurantRequestDto.getVegType());
        existingRestaurant.setRestaurantStyle(restaurantRequestDto.getRestaurantStyle());
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
