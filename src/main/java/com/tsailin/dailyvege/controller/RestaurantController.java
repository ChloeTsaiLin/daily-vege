package com.tsailin.dailyvege.controller;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class RestaurantController {

    @Autowired
    private RestaurantRepository repository;

    @PostMapping("/restaurants")
    public String create(@RequestBody Restaurant restaurant){
        repository.save(restaurant);

        return "restaurant created.";
    }

    @GetMapping("/restaurants/{restaurantId}")
    public String readById(@PathVariable String restaurantId){

        Long id = Long.valueOf(restaurantId);
        if(repository.existsById(id)){
            return "get restaurant by id.";
        } else{
            return "NOT FOUND.";
        }
    }

}
