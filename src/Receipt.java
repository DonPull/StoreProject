import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int number;
    private final Cashier cashier;
    private final LocalDateTime timestamp;
    private final List<LineItem> items;
    private final double total;

    public Receipt(int number, Cashier cashier, LocalDateTime timestamp, List<LineItem> items, double total) {
        this.number = number;
        this.cashier = cashier;
        this.timestamp = timestamp;
        this.items = items;
        this.total = total;
    }

    public static class LineItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Product product;
        private final int quantity;
        private final double price;

        public LineItem(Product product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }

    public int getNumber() { return number; }
    public Cashier getCashier() { return cashier; }
    public LocalDateTime getLocalDateTime() { return timestamp; }
    public List<LineItem> getLineItems() { return items; }
    public double getTotal() { return total; }
}