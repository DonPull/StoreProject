import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private double accumulatedDeliveryCosts = 0;

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
        this.accumulatedDeliveryCosts += product.getCostPrice() * product.getQuantity();
        inventory.put(product.getId(), product);
    }

    public Receipt sell(String cashierId, Map<String, Integer> order) throws Exception {
        Cashier cashier = cashiers.stream()
                .filter(c -> c.getId().equals(cashierId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid cashier: " + cashierId));

        List<Receipt.LineItem> items = new ArrayList<>();
        double orderFinalPrice = 0;

        // go tru every product in the order
        for (Map.Entry<String, Integer> entry : order.entrySet()) {
            String productId = entry.getKey();
            int quantityDemanded = entry.getValue();
            Product product = inventory.get(productId);

            if (product == null) throw new IllegalArgumentException("Unknown product ID: " + productId);
            if (product.getQuantity() < quantityDemanded) {
                throw new InsufficientQuantityException(String.format("Product %s (ID: %s) missing %d units. Available: %d, Demanded: %d",
                        product.getName(), productId, quantityDemanded - product.getQuantity(), product.getQuantity(), quantityDemanded));
            }

            // check if product has expired and cannot be sold
            if (product.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                throw new IllegalStateException("Cannot sell expired product: " + product.getName() + " (ID: " + productId + ")");
            }

            double markupPercent = product.getCategory() == Category.FOOD ? foodMarkup : nonFoodMarkup;
            double finalPricePerUnit = product.getSellingPrice(markupPercent, discountThresholdDays, discountPercent);
            items.add(new Receipt.LineItem(product, quantityDemanded, finalPricePerUnit));
            orderFinalPrice += finalPricePerUnit * quantityDemanded;
        }

        // if all checks have passed sell the product (decreate it's quantity)
        for (Receipt.LineItem item : items) {
            Product productInStock = inventory.get(item.getProduct().getId());
            productInStock.reduceQuantity(item.getQuantity());
        }

        // create receipt
        receiptCounter++;
        Receipt r = new Receipt(receiptCounter, cashier, LocalDateTime.now(), items, orderFinalPrice);
        receipts.put(receiptCounter, r);
        totalTurnover += orderFinalPrice;
        displayAndSaveReceipt(r);
        return r;
    }

    private void displayAndSaveReceipt(Receipt r) throws IOException {
        StringBuilder receiptContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        receiptContent.append("-----------------------------------\n");
        receiptContent.append("Receipt #: ").append(r.getNumber()).append("\n");
        receiptContent.append("Cashier: ").append(r.getCashier().getName()).append(" (ID: ").append(r.getCashier().getId()).append(")\n");
        receiptContent.append("Date: ").append(r.getLocalDateTime().format(formatter)).append("\n");
        receiptContent.append("Items:\n");
        for (Receipt.LineItem li : r.getLineItems()) {
            receiptContent.append(String.format(" - %s (ID: %s) x%d @ %.2f (Unit Price)%n",
                    li.getProduct().getName(), li.getProduct().getId(), li.getQuantity(), li.getPrice()));
        }
        receiptContent.append(String.format("Total: %.2f%n", r.getTotal()));
        receiptContent.append("-----------------------------------\n");

        // print receipt in console
        System.out.println("\n--- New Receipt Generated ---");
        System.out.println(receiptContent.toString());

        // save receipt in file
        String fileNameTxt = "receipt_" + r.getNumber() + ".txt";
        try (PrintWriter out = new PrintWriter(new FileWriter(fileNameTxt))) {
            out.println(receiptContent.toString());
        }

        // serialize
        String fileNameSer = "receipt_" + r.getNumber() + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileNameSer))) {
            oos.writeObject(r);
        }
    }

    public int getTotalReceipts() { return receiptCounter; }
    public double getTotalTurnover() { return totalTurnover; }
    public Map<String, Product> getInventory() { return Collections.unmodifiableMap(inventory); }
    public String getName() { return name; }
    public List<Cashier> getCashiers() { return Collections.unmodifiableList(cashiers); }

    public double getTotalSalaries() {
        return cashiers.stream().mapToDouble(Cashier::getSalary).sum();
    }

    public double getTotalDeliveryCosts() {
        return accumulatedDeliveryCosts;
    }

    public double getTotalExpenses() {
        return getTotalSalaries() + getTotalDeliveryCosts();
    }

    public double getProfit() {
        return totalTurnover - getTotalExpenses();
    }

    public Receipt loadSerializedReceipt(int number) throws IOException, ClassNotFoundException {
        String fileNameSer = "receipt_" + number + ".ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileNameSer))) {
            return (Receipt) ois.readObject();
        }
    }
    
    public String readTextReceipt(int number) throws IOException {
        String fileNameTxt = "receipt_" + number + ".txt";
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileNameTxt))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }
        return contentBuilder.toString();
    }
}