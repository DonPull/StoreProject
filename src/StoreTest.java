import org.junit.jupiter.api.*;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {
    private Store store;
    private Cashier cashier;

    @BeforeEach
    public void setup() {
        store = new Store("TestShop", 10.0, 20.0, 3, 5.0);
        cashier = new Cashier("T1", "TestCashier", 1000.0);
        store.addCashier(cashier);
        store.loadProduct(new Product("P1", "Bread", 1.0, Category.FOOD, LocalDate.now().plusDays(5), 10));
        store.loadProduct(new Product("P2", "Soap", 2.0, Category.NON_FOOD, LocalDate.now().plusDays(365), 5));
    }

    @AfterEach
    public void cleanup() {
        // clean up generated files
        for (int i = 1; i <= store.getTotalReceipts(); i++) {
            new File("receipt_" + i + ".txt").delete();
            new File("receipt_" + i + ".ser").delete();
        }
    }

    @Test
    public void testFoodMarkupOnly() {
        Product food = new Product("F1", "Milk", 1.5, Category.FOOD, LocalDate.now().plusDays(10), 5);
        double price = food.getSellingPrice(10.0, 3, 5.0);
        assertEquals(1.65, price, 0.001, "Food price with 10% markup should be costPrice * 1.1");
    }

    @Test
    public void testNonFoodMarkupOnly() {
        Product nf = new Product("NF1", "Book", 10.0, Category.NON_FOOD, LocalDate.now().plusDays(10), 5);
        double price = nf.getSellingPrice(20.0, 3, 5.0);
        assertEquals(12.0, price, 0.001, "Non-food price with 20% markup should be costPrice * 1.2");
    }

    @Test
    public void testDiscountAppliedWhenNearExpiry() {
        Product nearExpire = new Product("F2", "Yogurt", 2.0, Category.FOOD, LocalDate.now().plusDays(2), 5);
        double price = nearExpire.getSellingPrice(10.0, 3, 10.0);
        // costPrice * 1.1 = 2.2, then 10% discount => 1.98
        assertEquals(1.98, price, 0.001, "Discount should apply when daysToExpire < threshold");
    }

    @Test
    public void testNoDiscountWhenFarFromExpiry() {
        Product fresh = new Product("F3", "Cheese", 3.0, Category.FOOD, LocalDate.now().plusDays(10), 5);
        double price = fresh.getSellingPrice(10.0, 3, 10.0);
        assertEquals(3.3, price, 0.001, "No discount when daysToExpire >= threshold");
    }

    @Test
    public void testExpiredProductThrows() {
        Product expired = new Product("F4", "OldMilk", 1.0, Category.FOOD, LocalDate.now().minusDays(1), 5);
        assertThrows(IllegalStateException.class, () -> expired.getSellingPrice(10.0, 3, 5.0));
    }

    @Test
    public void testInsufficientQuantityException() {
        Map<String,Integer> order = Map.of("P1", 20);
        assertThrows(InsufficientQuantityException.class, () -> store.sell(cashier.getId(), order));
    }

    @Test
    public void testSuccessfulSaleReducesQuantity() throws Exception {
        Map<String,Integer> order = Map.of("P1", 3);
        int beforeQty = store.getInventory().get("P1").getQuantity();
        store.sell(cashier.getId(), order);
        int afterQty = store.getInventory().get("P1").getQuantity();
        assertEquals(beforeQty - 3, afterQty, "Quantity should reduce by sold amount");
    }

    @Test
    public void testReceiptSavedAndLoaded() throws Exception {
        Map<String,Integer> order = Map.of("P2", 2);
        Receipt r = store.sell(cashier.getId(), order);
        Receipt loaded = store.loadSerializedReceipt(r.getNumber());
        assertEquals(r.getNumber(), loaded.getNumber());
        assertEquals(r.getTotal(), loaded.getTotal(), 0.001);
        assertEquals(r.getCashier().getId(), loaded.getCashier().getId());
    }

    @Test
    public void testAnalytics() throws Exception {
        // initial: no receipts
        assertEquals(0, store.getTotalReceipts());
        assertEquals(0.0, store.getTotalTurnover(), 0.001);
        // sell two receipts
        store.sell(cashier.getId(), Map.of("P1", 1));
        store.sell(cashier.getId(), Map.of("P2", 1));
        assertEquals(2, store.getTotalReceipts(), "Total receipts should count two sales");
        assertTrue(store.getTotalTurnover() > 0, "Turnover should be positive");
        double expenses = store.getTotalCosts();
        assertTrue(expenses > 0, "Costs should include salaries and delivery costs");
        double profit = store.getProfit();
        assertEquals(store.getTotalTurnover() - expenses, profit, 0.001, "Profit should be turnover minus costs");
    }
}