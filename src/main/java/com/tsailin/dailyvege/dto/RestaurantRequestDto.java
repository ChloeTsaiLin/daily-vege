package com.tsailin.dailyvege.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestaurantRequestDto {

    private String vegType;             //TODO Enum
    private String restaurantStyle;

}
