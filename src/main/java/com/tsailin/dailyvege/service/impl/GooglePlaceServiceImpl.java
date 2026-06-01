package com.tsailin.dailyvege.service.impl;

import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.entity.RestaurantGoogleSource;
import com.tsailin.dailyvege.repository.GoogleSourceRepository;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.GooglePlaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
public class GooglePlaceServiceImpl implements GooglePlaceService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    private final RestaurantRepository restaurantRepository;
    private final GoogleSourceRepository googleSourceRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    String fieldMaskBasic = "id,displayName,formattedAddress,types,location";
        String fieldMaskPro = "regularOpeningHours,rating,priceLevel,servesVegetarianFood";
        String fieldMask = fieldMaskBasic + "," + fieldMaskPro;

    public GooglePlaceServiceImpl(RestaurantRepository restaurantRepository,
                                  GoogleSourceRepository googleSourceRepository) {
        this.restaurantRepository = restaurantRepository;
        this.googleSourceRepository = googleSourceRepository;
    }

    @Override
    @Transactional
    public Restaurant importGoogleRestaurant(String placeId) {

        log.info("Initiating Google Restaurant import for Place ID: {}", placeId);
        String url = "https://places.googleapis.com/v1/places/" + placeId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMask);         //TODO

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> googleData = (Map<String, Object>) response.getBody();

            if (googleData == null || googleData.isEmpty()) {
                log.warn("Google API returned an empty response for Place ID: {}", placeId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found from Google API.");
            }

            Restaurant newRestaurant = new Restaurant();
            Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);

            log.debug("Fetched data content: {}", googleData);
            RestaurantGoogleSource newSource = new RestaurantGoogleSource();
            newSource.setRestaurant(savedRestaurant);
            newSource.setGooglePlaceId(placeId);
            newSource.setCreatedDate(OffsetDateTime.now());

            mapGoogleDataToEntity(googleData, newSource);
            googleSourceRepository.save(newSource);

            log.info("Successfully imported restaurant. Generated Core Restaurant ID: {}", savedRestaurant.getId());
            return savedRestaurant;

        } catch (Exception e) {
            log.error("Failed to import Google restaurant for Place ID: {}", placeId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to import restaurant due to an external API or database error."
            );
        }
    }

    @Override
    @Transactional
    public Restaurant syncGoogleRestaurant(Long id) {

        log.info("Initiating synchronization for local Restaurant ID: {}", id);

        // check exist
        RestaurantGoogleSource existingSource = googleSourceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Synchronization aborted. Local Restaurant ID {} not found.", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Cannot synchronize: Restaurant with the given ID does not exist."
                    );
                });

        // check response
        try {
            Map<String, Object> googleData = fetchDetailsFromGoogle(existingSource.getGooglePlaceId());

            if (googleData == null || googleData.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google API returned empty response.");
            }

            mapGoogleDataToEntity(googleData, existingSource);
            googleSourceRepository.save(existingSource);

            log.info("DB save successful. Synchronized Core Restaurant ID: {}", id);

            return existingSource.getRestaurant();

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            log.error("Synchronization failed unexpectedly for local ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google API 呼叫失敗: " + e.getMessage());
        }
    }


    public void mapGoogleDataToEntity(Map<String, Object> googleData, RestaurantGoogleSource source) {

        Map<String, String> displayNameMap = (Map<String, String>) googleData.get("displayName");
        if (displayNameMap != null) {
            source.setDisplayName(displayNameMap.get("text"));
        }

        source.setFormattedAddress((String) googleData.get("formattedAddress"));

        if (googleData.containsKey("location")
                && googleData.get("location") instanceof Map<?, ?> locationMap) {

            Object latObj = locationMap.get("latitude");
            Object lngObj = locationMap.get("longitude");

            if (latObj != null) {
                source.setLatitude(Double.valueOf(latObj.toString()));
            }
            if (lngObj != null) {
                source.setLongitude(Double.valueOf(lngObj.toString()));
            }
        }

        if (googleData.containsKey("rating")) {
            source.setRating((Double) googleData.get("rating"));
        }

        if (googleData.containsKey("types") && googleData.get("types") != null) {
            Object typesObj = googleData.get("types");

            if (typesObj instanceof List<?>) {
                ArrayList<String> typesList = new ArrayList<>();
                for(Object type : (List<?>) typesObj){
                    if(type instanceof String){
                        typesList.add(type.toString());
                    }
                }

                if (!typesList.isEmpty()) {
                    String joinedTypes = String.join(",", typesList);
                    source.setRawTypes(joinedTypes);
                }
            }
        }

        if (googleData.containsKey("priceLevel")) {
            source.setPriceLevel(googleData.get("priceLevel").toString());
        }

        if(googleData.containsKey("servesVegetarianFood")){
            source.setServesVegetarianFood((boolean)googleData.get("servesVegetarianFood"));
        }

        if (googleData.containsKey("regularOpeningHours")
                && googleData.get("regularOpeningHours") instanceof Map<?, ?> openingHoursList) {

                if (openingHoursList.get("weekdayDescriptions") instanceof List<?> weekdayDesList) {
                    List<String> weekdayList = new ArrayList<>();
                    for (Object weekdayDes : weekdayDesList) {
                        if (weekdayDes instanceof String) {
                            weekdayList.add(weekdayDes.toString());
                        }
                    }

                    if (!weekdayList.isEmpty()) {
                        String joinedHours = String.join("|", weekdayList);
                        source.setWeekdayDescriptions(joinedHours);
                    }
                }
        }

        source.setLastModifiedDate(OffsetDateTime.now());
    }

    private Map<String, Object> fetchDetailsFromGoogle(String googlePlaceId) {
        String url = "https://places.googleapis.com/v1/places/" + googlePlaceId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMask);         //TODO

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("HTTP RestTemplate call to Google Places API failed for Place ID: {}", googlePlaceId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch details from Google API.");
        }
    }

}
