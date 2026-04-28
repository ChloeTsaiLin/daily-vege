package com.tsailin.dailyvege.controller;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Validated
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> create(@RequestBody @Valid Restaurant restaurantRequest){

        Long id = restaurantService.saveRestaurant(restaurantRequest);
        Restaurant restaurant = restaurantService.getRestaurantById(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Object> readById(@PathVariable Long restaurantId) {

        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant != null) {
            return ResponseEntity.status(HttpStatus.OK).body(restaurant);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Object> updateById(@PathVariable Long restaurantId,
                                             @RequestBody @Valid Restaurant restaurantRequest) {

        Long id = restaurantService.updateRestaurant(restaurantId, restaurantRequest);
        Restaurant updateRestaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.status(HttpStatus.OK).body(updateRestaurant);
    }

    @DeleteMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Object> deleteById(@PathVariable Long restaurantId){
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
