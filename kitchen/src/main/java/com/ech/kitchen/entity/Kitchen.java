package com.ech.kitchen.entity;



import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ech.kitchen.entity.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.entity.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.entity.ShelfTemperatureEnum.Frozen;
import static com.ech.kitchen.entity.ShelfTemperatureEnum.Hot;

@Component
public class Kitchen {

    private final Map<ShelfTemperatureEnum, Shelf> shelfMap = new HashMap<>();
;

    public Kitchen() {
        buildShelfs();
    }

    private void buildShelfs() {
        final Shelf coldShelf = Shelf.builder().allowableTemperature(Cold).build();
        shelfMap.put(Cold, coldShelf);

        final Shelf hotShelf = Shelf.builder().allowableTemperature(Hot).build();
        shelfMap.put(Hot, hotShelf);

        final Shelf frozenShelf = Shelf.builder().allowableTemperature(Frozen).build();
        shelfMap.put((Frozen), frozenShelf);

        final Shelf overflowShelf = Shelf.builder().allowableTemperature(Any).build();
        shelfMap.put(Any, overflowShelf);
    }

}


