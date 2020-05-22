package com.ech.kitchen.mo;

import com.ech.order.mo.Order;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static com.ech.order.mo.OrderTemperatureEnum.hot;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShelfTest {

    @Test
    public void testAddOrders() {
        final int maxCapacity = 10;
        final int orderNumb = 3;

        Shelf shelf = new Shelf(maxCapacity, Hot);
        final Map<Boolean, Long> resultStatusCount = IntStream.range(0, orderNumb)
                .mapToObj(intToCookedOrderFunction())
                .map(addOrderIntoShelf(shelf)).collect(Collectors.groupingBy(identity(), counting()));

        assertEquals(orderNumb, resultStatusCount.get(true));
        assertEquals(null, resultStatusCount.get(false));
        assertEquals(orderNumb, shelf.currentOrderNumb());
    }

    private IntFunction<CookedOrder> intToCookedOrderFunction() {
        return i -> new CookedOrder(new Order("hotOrder" + i, hot));
    }

    @Test
    public void testAddOrderExceedMax() {
        final int maxCapacity = 10;
        final int orderNumb = 12;
        final int expectedFailureTimes = orderNumb - maxCapacity;

        Shelf shelf = new Shelf(maxCapacity, Hot);
        final Map<Boolean, Long> resultStatusCount = IntStream.range(0, orderNumb)
                .mapToObj(intToCookedOrderFunction())
                .map(addOrderIntoShelf(shelf))
                .collect(Collectors.groupingBy(identity(), counting()));

        assertEquals(maxCapacity, resultStatusCount.get(true));
        assertEquals(expectedFailureTimes, resultStatusCount.get(false));
        assertEquals(maxCapacity, shelf.currentOrderNumb());
    }

    @Test
    public void testAddOrderExceedMaxThrowException() {
        final int maxCapacity = 10;
        Shelf shelf = new Shelf(maxCapacity, Hot);
        addSomeOrderInShelf(maxCapacity, shelf);

        assertThrows(ShelfFullException.class, () -> shelf.add(new CookedOrder(new Order("hotOrder11", hot))));
    }

    private void addSomeOrderInShelf(int amount, Shelf shelf) {
        IntStream.range(0, amount)
                .mapToObj(intToCookedOrderFunction())
                .forEach(order -> {
                    try {
                        shelf.add(order);
                    } catch (ShelfFullException e) {
                        assertTrue(true, "Should not throw exception while there's space enough.");
                    }
                });
    }

    @Test
    public void testAddAndRemoveOrder() throws ShelfFullException {
        final int maxCapacity = 10;
        CookedOrder cookedOrder1 = new CookedOrder(new Order("hotOrder1", hot));
        CookedOrder cookedOrder2 = new CookedOrder(new Order("hotOrder2", hot));
        Shelf shelf = new Shelf(maxCapacity, Hot);

        assertEquals(0, shelf.currentOrderNumb());

        shelf.add(cookedOrder1);
        assertEquals(1, shelf.currentOrderNumb());
        shelf.add(cookedOrder2);
        assertEquals(2, shelf.currentOrderNumb());

        shelf.remove(cookedOrder1);
        assertEquals(1, shelf.currentOrderNumb());
        shelf.remove(cookedOrder2);
        assertEquals(0, shelf.currentOrderNumb());
    }

    private Function<CookedOrder, Boolean> addOrderIntoShelf(Shelf shelf) {
        return cookedOrder -> {
            try {
                shelf.add(cookedOrder);
                return true;
            } catch (ShelfFullException e) {
                return false;
            }
        };
    }
}
