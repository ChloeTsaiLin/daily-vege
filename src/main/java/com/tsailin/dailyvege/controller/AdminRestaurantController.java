package com.tsailin.dailyvege.controller;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.service.GooglePlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final GooglePlaceService googlePlaceService;

    //TODO Spring Security: 非Admin 403
    /**
     * Business Logic: Aborts with 409 Conflict if already exists; creates if new.
     */
    @PostMapping("/import")
    public ResponseEntity<Restaurant> importNewRestaurant(@RequestParam String placeId) {

        log.info("REST request to import restaurant from Google Place ID: {}", placeId);
        Restaurant restaurant = googlePlaceService.importGoogleRestaurant(placeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    /**
     * Business Logic: Aborts with 404 Not Found if local record does not exist.
     */
    @PutMapping("/{id}/sync")
    public ResponseEntity<Void> syncExistingRestaurant(@PathVariable Long id) {

        log.info("REST request to synchronize restaurant with local ID: {}", id);
        googlePlaceService.syncGoogleRestaurant(id);

        return ResponseEntity.noContent().build();
    }

}
