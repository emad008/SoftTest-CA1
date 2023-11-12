package domain;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineTest {
    private Engine engine;
    @Before
    public void setUp() {
        engine = new Engine();
    }

    @Test
    public void testGetAverageOrderQuantityByCustomer() {
        Order order1 = new Order(1, 1, 100, 5);
        Order order2 = new Order(2, 1, 150, 8);
        Order order3 = new Order(3, 2, 15, 1);

        engine.orderHistory.add(order1);
        engine.orderHistory.add(order2);
        engine.orderHistory.add(order3);

        int average = engine.getAverageOrderQuantityByCustomer(1);
        assertEquals(6, average);
    }

    @Test
    public void testGetAverageOrderQuantityByCustomerNoOrders() {
        int average = engine.getAverageOrderQuantityByCustomer(1);
        assertEquals(0, average);
    }

    @Test
    public void testGetQuantityPatternByPrice() {
        Order order1 = new Order(1, 1, 100, 5);
        Order order2 = new Order(2, 1, 100, 8);
        Order order3 = new Order(3, 1, 200, 11);

        engine.orderHistory.add(order1);
        engine.orderHistory.add(order2);
        engine.orderHistory.add(order3);

        int quantityPattern = engine.getQuantityPatternByPrice(100);

        assertEquals(3, quantityPattern);
    }

    @Test
    public void testGetQuantityPatternByPriceNoOrders() {
        int quantityPattern = engine.getQuantityPatternByPrice(100);
        assertEquals(0, quantityPattern);
    }

    @Test
    public void testGetCustomerFraudulentQuantity() {
        Order order1 = new Order(1, 1, 100, 8);

        engine.orderHistory.add(order1);

        int fraudulentQuantity = engine.getCustomerFraudulentQuantity(order1);

        assertEquals(0, fraudulentQuantity);
    }

    @Test
    public void testGetCustomerFraudulentQuantityEqual() {
        Order order1 = new Order(1, 1, 100, 5);

        engine.orderHistory.add(order1);

        int fraudulentQuantity = engine.getCustomerFraudulentQuantity(order1);

        assertEquals(0, fraudulentQuantity);
    }

    @Test
    public void testAddOrderAndGetFraudulentQuantity() {
        Order order1 = new Order(1, 1, 100, 8);

        int fraudulentQuantity = engine.addOrderAndGetFraudulentQuantity(order1);

        assertEquals(8, fraudulentQuantity);
    }

    @Test
    public void testAddOrderAndGetFraudulentQuantityExistingOrder() {
        Order order1 = new Order(1, 1, 100, 8);

        engine.orderHistory.add(order1);

        int fraudulentQuantity = engine.addOrderAndGetFraudulentQuantity(order1);

        assertEquals(0, fraudulentQuantity);
    }

    @Test
    public void testAddOrderAndGetFraudulentQuantityWithZeroQuantityOrder() {
        Order order1 = new Order(1, 1, 100, 0);

        int fraudulentQuantity = engine.addOrderAndGetFraudulentQuantity(order1);

        assertEquals(0, fraudulentQuantity);
    }

    @Test
    public void testOrderEqualCheckForTwoEqualOrders() {
        assertEquals(new Order(1, 1, 100, 0), new Order(1, 1, 100, 1));
    }

    @Test
    public void testOrderEqualCheckForTwoNotEqualOrders() {
        assertNotEquals(new Order(2, 1, 100, 0), new Order(1, 1, 100, 1));
    }

    @Test
    public void testOrderEqualCheckForTwoNotDifferentTypeObjects() {
        assertNotEquals(new Order(2, 1, 100, 0), 1);
    }

    @Test
    public void testOrderSettersAndGetters() {
        Order order1 = new Order();
        order1.setId(1);
        order1.setCustomer(1);
        order1.setPrice(100);
        order1.setQuantity(5);
        assertEquals(order1.getId(), 1);
        assertEquals(order1.getCustomer(), 1);
        assertEquals(order1.getPrice(), 100);
        assertEquals(order1.getQuantity(), 5);
    }
}
