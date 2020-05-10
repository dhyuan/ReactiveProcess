package com.ech.order;

import com.fasterxml.jackson.annotation.JsonProperty;

//@lombok.Data
public class Order {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private TemperatureEnum temp;

    @JsonProperty
    private Integer shelfLife;

    @JsonProperty
    private Float decayRate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemperatureEnum getTemp() {
        return temp;
    }

    public void setTemp(TemperatureEnum temp) {
        this.temp = temp;
    }

    public Integer getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(Integer shelfLife) {
        this.shelfLife = shelfLife;
    }

    public Float getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(Float decayRate) {
        this.decayRate = decayRate;
    }
}
