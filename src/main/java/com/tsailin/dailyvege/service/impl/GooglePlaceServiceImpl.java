package com.tsailin.dailyvege.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tsailin.dailyvege.entity.Restaurant;
import com.tsailin.dailyvege.entity.RestaurantGoogleSource;
import com.tsailin.dailyvege.repository.GoogleSourceRepository;
import com.tsailin.dailyvege.repository.RestaurantRepository;
import com.tsailin.dailyvege.service.GooglePlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class GooglePlaceServiceImpl implements GooglePlaceService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private GoogleSourceRepository googleSourceRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public GooglePlaceServiceImpl(RestaurantRepository restaurantRepository,
                            GoogleSourceRepository googleSourceRepository) {
        this.restaurantRepository = restaurantRepository;
        this.googleSourceRepository = googleSourceRepository;
    }


    @Transactional
    public Restaurant importGoogleRestaurant(String placeId) {

        String url = "https://places.googleapis.com/v1/places/" + placeId;
        String fieldMaskBasic = "id,displayName,formattedAddress,types,location";
//        String fieldMaskPro = "regularOpeningHours,rating,priceLevel,servesVegetarianFood";
//        String fieldMask = fieldMaskBasic + "," + fieldMaskPro;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMaskBasic);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(response.getBody());
            System.out.println("抓取的資料內容: " + prettyJson);

            // convert json to Map<String, Object>
            Map<String, Object> mockGoogleData = objectMapper.readValue(
                    prettyJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            System.out.println("Successfully parsed JSON to Map. Preparing DB write...");

            Restaurant newRestaurant = new Restaurant();
            Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);

            RestaurantGoogleSource newSource = new RestaurantGoogleSource();
            newSource.setRestaurant(savedRestaurant);
            newSource.setGooglePlaceId(placeId);
            newSource.setCreatedDate(OffsetDateTime.now());

            mapGoogleDataToEntity(mockGoogleData, newSource);
            googleSourceRepository.save(newSource);

            if (savedRestaurant.getId() != null) {
                System.out.println("Generated Core Restaurant ID: " + savedRestaurant.getId());
            } else {
                System.err.println("Save failed: Service returned null.");
            }

            return savedRestaurant;

        } catch (Exception e) {
            System.err.println("Google API call failed: " + e.getMessage());
        }

        return new Restaurant();
    }


    @Override
    public Restaurant syncGoogleRestaurant(Long id) {
        RestaurantGoogleSource existingSource = googleSourceRepository.findById(id)
                .orElseThrow();

        try {
            Map<String, Object> googleData = fetchDetailsFromGoogle(existingSource.getGooglePlaceId());

            if (googleData == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google API returned empty response.");
            }

            System.out.println("Successfully parsed JSON to Map. Preparing DB write...");
            mapGoogleDataToEntity(googleData, existingSource);
            googleSourceRepository.save(existingSource);

            System.out.println("DB save successful. Generated Core Restaurant ID: " + id);

            return existingSource.getRestaurant();

        } catch (Exception e) {
            System.err.println("Synchronization failed: " + e.getMessage());
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
        String fieldMaskBasic = "id,displayName,formattedAddress,types,location";
//        String fieldMaskPro = "regularOpeningHours,rating,priceLevel,servesVegetarianFood";
//        String fieldMask = fieldMaskBasic + "," + fieldMaskPro;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMaskBasic);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> result = response.getBody();
        System.out.println(result);

        return result;
    }

}
