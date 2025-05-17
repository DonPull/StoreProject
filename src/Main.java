import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        // STORE 1
        Store store = new Store("MyShop", 20.0, 40.0, 5, 10.0);
        store.addCashier(new Cashier("C1", "Alice", 1200));
        store.addCashier(new Cashier("C2", "Bob", 1100));

        store.loadProduct(new Product("P1", "Milk", 1.0, Category.FOOD, LocalDate.now().plusDays(3), 50));
        store.loadProduct(new Product("P2", "TV", 200.0, Category.NON_FOOD, LocalDate.now().plusYears(1), 10));

        Map<String, Integer> order = new HashMap<>();
        order.put("P1", 2);
        order.put("P2", 1);
        Receipt receipt = store.sell("C1", order);
        System.out.println("Sold! Receipt #: " + receipt.getNumber());

        Map<String, Integer> order1 = new HashMap<>();
        order1.put("P1", 20);
        order1.put("P2", 5);
        Receipt receipt1 = store.sell("C1", order1);
        System.out.println("Sold! Receipt #: " + receipt1.getNumber());

        Map<String, Integer> order2 = new HashMap<>();
        order2.put("P1", 28);
        order2.put("P2", 4);
        Receipt receipt2 = store.sell("C2", order2);
        System.out.println("Sold! Receipt #: " + receipt2.getNumber());

        System.out.println("Total receipts: " + store.getTotalReceipts());
        System.out.println("Profit: " + store.getProfit());

        // STORE 2
        Store store1 = new Store("MyShop_1", 10.0, 20.0, 3, 15.0);
        store1.addCashier(new Cashier("C1", "Kris", 2200));
        store1.addCashier(new Cashier("C2", "Angelina", 2100));

        store1.loadProduct(new Product("P1", "Bread", 5.0, Category.FOOD, LocalDate.now().plusDays(2), 50));
        store1.loadProduct(new Product("P2", "Soda", 6.0, Category.FOOD, LocalDate.now().plusDays(20), 50));
        store1.loadProduct(new Product("P3", "Laptop", 2000.0, Category.NON_FOOD, LocalDate.now().plusYears(1), 10));

        Map<String, Integer> order_1 = new HashMap<>();
        order_1.put("P1", 5);
        order_1.put("P2", 2);
        order_1.put("P3", 1);
        Receipt receipt_1 = store1.sell("C1", order_1);
        System.out.println("Sold! Receipt #: " + receipt_1.getNumber());

        Map<String, Integer> order_2 = new HashMap<>();
        order_2.put("P1", 45);
        order_2.put("P2", 48);
        order_2.put("P3", 9);
        Receipt receipt_2 = store1.sell("C1", order_2);
        System.out.println("Sold! Receipt #: " + receipt_2.getNumber());

        // this should throw an exception because there is not more bread
        Map<String, Integer> order_3 = new HashMap<>();
        order_3.put("P1", 1);
        Receipt receipt_3 = store1.sell("C2", order_3);
        System.out.println("Sold! Receipt #: " + receipt_3.getNumber());

        System.out.println("Total receipts: " + store1.getTotalReceipts());
        System.out.println("Profit: " + store1.getProfit());
    }
}