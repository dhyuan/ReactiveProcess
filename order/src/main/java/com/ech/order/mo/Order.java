package com.ech.order.mo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Order {

    @Getter @Setter
    @JsonProperty
    private String id;

    @Getter @Setter
    @JsonProperty
    private String name;

    @Getter @Setter
    @JsonProperty
    private OrderTemperatureEnum temp;

    @Getter @Setter
    @JsonProperty
    private Integer shelfLife;

    @Getter @Setter
    @JsonProperty
    private Float decayRate;

    public Order(String name, OrderTemperatureEnum temp) {
        this.name = name;
        this.temp = temp;
    }
}
