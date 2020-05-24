package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.ICookedOrderPickStrategy;
import org.springframework.stereotype.Component;

import java.time.chrono.MinguoChronology;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

@Component
public class CookedOrderRandomPicker implements ICookedOrderPickStrategy {

    private final Random random = new Random();

    @Override
    public Optional<CookedOrder> pickupFrom(List<Shelf> shelves) {
        final int[] randomIndexes = shuffleIndex(shelves.size());
        for (int index : randomIndexes) {
            final Optional<CookedOrder> cookedOrder = shelves.get(index).pullOrder();
            if (cookedOrder.isPresent()) {
                return cookedOrder;
            }
        }
        return Optional.empty();
    }

    public int[] shuffleIndex(int size) {
        Random rand = new Random();
        final int[] indexes = IntStream.range(0, size).toArray();
        for (int i = 0; i < size; i++) {
            swapAt(indexes, i, rand.nextInt(size));
        }
        return indexes;
    }

    private void swapAt(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
