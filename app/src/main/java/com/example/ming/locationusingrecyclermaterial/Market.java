package com.example.ming.locationusingrecyclermaterial;

/**
 * Created by ming on 4/10/16.
 */

public class Market {

    private String name, district, state, distance;

    public Market(String name, String state, String district, String distance) {
        this.setName(name);
        this.setState(state);
        this.setDistance(distance);
        this.setDistrict(district);
    }


    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public String getState() {
        return state;
    }

    public String getDistance() {
        return distance;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
