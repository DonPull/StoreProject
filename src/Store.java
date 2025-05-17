import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class Store {
    private final String name;
    private final double foodMarkup;
    private final double nonFoodMarkup;
    private final long discountThresholdDays;
    private final double discountPercent;

    private final Map<String, Product> inventory = new HashMap<>();
    private final Map<Integer, Receipt> receipts = new HashMap<>();
    private final List<Cashier> cashiers = new ArrayList<>();

    private int receiptCounter = 0;
    private double totalTurnover = 0;

    public Store(String name, double foodMarkup, double nonFoodMarkup, long discountThresholdDays, double discountPercent) {
        this.name = name;
        this.foodMarkup = foodMarkup;
        this.nonFoodMarkup = nonFoodMarkup;
        this.discountThresholdDays = discountThresholdDays;
        this.discountPercent = discountPercent;
    }

    public void addCashier(Cashier cashier) {
        cashiers.add(cashier);
    }

    public void loadProduct(Product product) {
        inventory.put(product.getId(), product);
    }

    public Receipt sell(String cashierId, Map<String, Integer> order) throws Exception {
        Cashier cashier = cashiers.stream()
                .filter(c -> c.getId().equals(cashierId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid cashier"));

        List<Receipt.LineItem> items = new ArrayList<>();
        double orderFinalPrice = 0;

        // go tru every product in the order
        for (Map.Entry<String, Integer> entry : order.entrySet()) {
            Product product = inventory.get(entry.getKey()); // retrieve the product from the HashMap using it's id that we get from the order
            int quantity = entry.getValue();
            if (product == null) throw new IllegalArgumentException("Unknown product: " + entry.getKey());
            if (product.getQuantity() < quantity) {
                throw new InsufficientQuantityException(String.format("Product %s missing %d units", product.getName(), quantity - product.getQuantity()));
            }
            double markupPercent = product.getCategory() == Category.FOOD ? foodMarkup : nonFoodMarkup;
            double finalPrice = product.getSellingPrice(markupPercent, discountThresholdDays, discountPercent);
            items.add(new Receipt.LineItem(product, quantity, finalPrice));
            orderFinalPrice += finalPrice * quantity;
        }

        // deduct from product quantity (because the customer just bought some)
        for (Receipt.LineItem item : items) {
            Product product = item.getProduct();
            product.reduceQuantity(item.getQuantity());
        }

        // create receipt
        receiptCounter++;
        Receipt r = new Receipt(receiptCounter, cashier, LocalDateTime.now(), items, orderFinalPrice);
        receipts.put(receiptCounter, r);
        totalTurnover += orderFinalPrice;
        saveReceipt(r);
        return r;
    }

    private void saveReceipt(Receipt r) throws IOException {
        String fileName = "receipt_" + r.getNumber() + ".txt";
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println("Receipt #: " + r.getNumber());
            out.println("Cashier: " + r.getCashier().getName());
            out.println("Date: " + r.getLocalDateTime());
            out.println("Items:");
            for (Receipt.LineItem li : r.getLineItems()) {
                out.printf(" - %s x%d @ %.2f%n", li.getProduct().getName(), li.getQuantity(), li.getPrice());
            }
            out.printf("Total: %.2f%n", r.getTotal());
        }
        // serialize
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("receipt_" + r.getNumber() + ".ser"))) {
            oos.writeObject(r);
        }
    }

    public int getTotalReceipts() { return receiptCounter; }
    public double getTotalTurnover() { return totalTurnover; }
    public Map<String, Product> getInventory() { return inventory; }
    public String getName() { return name; }

    public double getTotalSalaries() {
        return cashiers.stream().mapToDouble(Cashier::getSalary).sum();
    }

    public double getTotalCosts() {
        double deliveryCosts = inventory.values().stream()
                .mapToDouble(product -> product.getCostPrice() * product.getQuantity())
                .sum();
        return getTotalSalaries() + deliveryCosts;
    }

    public double getProfit() {
        return totalTurnover - getTotalCosts();
    }

    public Receipt loadSerializedReceipt(int number) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("receipt_" + number + ".ser"))) {
            return (Receipt) ois.readObject();
        }
    }
}