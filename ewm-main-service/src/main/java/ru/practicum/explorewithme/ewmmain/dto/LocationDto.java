package ru.practicum.explorewithme.ewmmain.dto;

import jakarta.validation.constraints.NotNull;

public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;

    public LocationDto() {
    }

    public LocationDto(Float lat, Float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }
}
