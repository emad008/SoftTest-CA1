package model;
import exceptions.CommodityIsNotInBuyList;
import exceptions.InsufficientCredit;
import exceptions.InvalidCreditRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    void setUp() throws InvalidCreditRange {
        user = new User("testUser", "password", "test@example.com", "01/01/1990", "123 Main St");
        user.addCredit(200.0f);
    }

    @ParameterizedTest
    @ValueSource(floats = { 0.0f, 100.0f, 1000.0f })
    void testAddCredit(float amount) throws InvalidCreditRange {
        user.addCredit(amount);
        assertEquals(200.0f + amount, user.getCredit(), 0.001);
    }

    @Test
    void testAddCreditWithNegativeAmount() {
        assertThrows(InvalidCreditRange.class, () -> user.addCredit(-50.0f));
    }

    @ParameterizedTest
    @ValueSource(floats = { 0.0f, 100.0f, 150.0f, 200.0f })
    void testWithdrawCredit(float amount) throws InsufficientCredit {
        user.withdrawCredit(amount);
        assertEquals(200.0f - amount, user.getCredit(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(floats = { 200.00001f, 1000.0f })
    void testWithdrawCreditWithInsufficientFunds(float amount) {
        assertThrows(InsufficientCredit.class, () -> user.withdrawCredit(amount));
    }

    @Test
    void testAddBuyItem() {
        Commodity commodity = new Commodity();
        commodity.setId("123");
        commodity.setName("Sample Item");
        commodity.setPrice(5);
        user.addBuyItem(commodity);

        assertTrue(user.getBuyList().containsKey("123"));
        assertEquals(1, user.getBuyList().get("123"));
    }

    @Test
    void testAddAlreadyInBuyListBuyItem() {
        Commodity commodity = new Commodity();
        commodity.setId("123");
        commodity.setName("Sample Item");
        commodity.setPrice(5);
        user.addBuyItem(commodity);
        user.addBuyItem(commodity);

        assertTrue(user.getBuyList().containsKey("123"));
        assertEquals(2, user.getBuyList().get("123"));
    }

    @Test
    void testAddPurchasedItem() {
        user.addPurchasedItem("456", 3);

        assertTrue(user.getPurchasedList().containsKey("456"));
        assertEquals(3, user.getPurchasedList().get("456"));
    }

    @Test
    void testAddAlreadyPurchasedPurchasedItem() {
        user.addPurchasedItem("456", 3);
        user.addPurchasedItem("456", 6);

        assertTrue(user.getPurchasedList().containsKey("456"));
        assertEquals(3 + 6, user.getPurchasedList().get("456"));
    }

    @Test
    void testRemoveItemFromBuyList() throws CommodityIsNotInBuyList {
        Commodity commodity = new Commodity();
        commodity.setId("789");
        commodity.setName("Another Item");
        commodity.setPrice(15);
        user.addBuyItem(commodity);
        user.removeItemFromBuyList(commodity);

        assertFalse(user.getBuyList().containsKey("789"));
    }

    @Test
    void testRemoveItemFromBuyListWithMoreThanOneOfSmaeItemInList() throws CommodityIsNotInBuyList {
        Commodity commodity = new Commodity();
        commodity.setId("999");
        commodity.setName("Non-existing Item");
        commodity.setPrice(5);
        user.addBuyItem(commodity);
        user.addBuyItem(commodity);
        user.removeItemFromBuyList(commodity);

        assertTrue(user.getBuyList().containsKey("999"));
        assertEquals(1, user.getBuyList().get("999"));
    }

    @Test
    void testRemoveItemFromBuyListCommodityNotInBuyList() {
        Commodity commodity = new Commodity();
        commodity.setId("999");
        commodity.setName("Non-existing Item");
        commodity.setPrice(5);
        assertThrows(CommodityIsNotInBuyList.class, () -> user.removeItemFromBuyList(commodity));
    }
}
