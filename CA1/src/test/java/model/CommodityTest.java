package model;

import exceptions.NotInStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommodityTest {

    private Commodity commodity;

    @BeforeEach
    void setUp() {
        commodity = new Commodity();
        commodity.setId("123");
        commodity.setName("Test Commodity");
        commodity.setInStock(10);
        commodity.setInitRate(3.0f);
    }

    @Test
    void testAddRate() {
        commodity.addRate("user1", 4);
        assertEquals(4, commodity.getUserRate().get("user1"));
    }

    @Test
    void testCalcRating() {
        commodity.addRate("user1", 4);
        commodity.addRate("user2", 5);
        assertEquals(4.0f, commodity.getRating(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(ints = { 5, 10, 15, 20, -10, -5, -1 })
    void testUpdateInStock(int amount) throws NotInStock {
        commodity.updateInStock(amount);
        assertEquals(10 + amount, commodity.getInStock());
    }

    @ParameterizedTest
    @ValueSource(ints = { -11, -12, -15, -20 })
    void testUpdateInStockWithNotEnoughStock(int amount) {
        assertThrows(NotInStock.class, () -> commodity.updateInStock(amount));
    }
}
