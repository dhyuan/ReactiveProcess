package com.ech.order.mo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Order {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private OrderTemperatureEnum temp;

    @JsonProperty
    private Integer shelfLife;

    @JsonProperty
    private Float decayRate;

    public Order(String name, OrderTemperatureEnum temp) {
        this.name = name;
        this.temp = temp;
    }
}
