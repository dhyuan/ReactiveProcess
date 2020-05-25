package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.ICookedOrderPickStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * This implementation choose a shelf randomly and get the first order in the shelf.
 * If the selected shelf is empty, then try to get cooked order from other shelves.
 * Return empty if there is no cooked order on any of the shelves.
 */
@Component
public class CookedOrderRandomPicker implements ICookedOrderPickStrategy {

    private final Random rand = new Random();

    /**
     * Randomly select a shelf and return the first order on this shelf.
     *
     * @param shelves List of shelves holding cooked orders.
     * @return return empty if there is no order available on the shelf.
     */
    @Override
    public Optional<CookedOrder> pickupFrom(List<Shelf> shelves) {
        final int[] randomIndexes = shuffleIndex(shelves.size());
        for (int index : randomIndexes) {
            final Optional<CookedOrder> cookedOrder = shelves.get(index).pullOrder();
            if (cookedOrder.isPresent()) {
                // find an order, return.
                return cookedOrder;
            }
        }
        // no cooked order available, return empty.
        return Optional.empty();
    }

    private int[] shuffleIndex(int size) {
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
