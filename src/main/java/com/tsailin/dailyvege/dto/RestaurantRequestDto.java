package com.tsailin.dailyvege.dto;

public class RestaurantRequestDto {

    private String vegType;             //TODO Enum
    private String restaurantStyle;

    public String getVegType() {
        return vegType;
    }

    public String getRestaurantStyle() {
        return restaurantStyle;
    }

    public void setVegType(String vegType) {
        this.vegType = vegType;
    }

    public void setRestaurantStyle(String restaurantStyle) {
        this.restaurantStyle = restaurantStyle;
    }
}
