package com.tsailin.dailyvege.repository;

import com.tsailin.dailyvege.entity.Restaurant;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {

    List<Restaurant> id(Long id);
}
