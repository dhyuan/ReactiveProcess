package com.ech.kitchen.mo;

import com.ech.kitchen.KitchenBaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Frozen;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KitchenTest extends KitchenBaseIT {

    @Autowired
    private Kitchen kitchen;

    @Test
    public void testCreateKitchen() {
        assertEquals(4, kitchen.getPickupArea().values().size());

        assertEquals(10, kitchen.getPickupArea().get(Hot).getMaxCapacity());
        assertEquals(10, kitchen.getPickupArea().get(Cold).getMaxCapacity());
        assertEquals(10, kitchen.getPickupArea().get(Frozen).getMaxCapacity());
        assertEquals(15, kitchen.getPickupArea().get(Any).getMaxCapacity());
    }
}
