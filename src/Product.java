import java.io.Serializable;
import java.time.LocalDate;

public class Product implements Serializable{
    private final String id;
    private final String name;
    private final double costPrice;
    private final Category category;
    private final LocalDate expiryDate;
    private int quantity;

    public Product(String id, String name, double costPrice, Category category, LocalDate expiryDate, int quantity) {
        this.id = id;
        this.name = name;
        this.costPrice = costPrice;
        this.category = category;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getCostPrice() { return costPrice; }
    public Category getCategory() { return category; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQuantity() { return quantity; }
    public void reduceQuantity(int amount) { this.quantity -= amount; }

    // compute selling price based on markup and potential close to expiry date discount.
    public double getSellingPrice(double markupPercent, long discountThresholdDays, double discountPercent) {
        double priceAfterMarkup = costPrice * (1 + markupPercent/100.0);
        long daysToExpire = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (daysToExpire < 0) {
            throw new IllegalStateException("Product expired: " + name);
        }
        if (daysToExpire < discountThresholdDays) {
            double discountPriceByDollarAmount = priceAfterMarkup * discountPercent/100.0;
            priceAfterMarkup = priceAfterMarkup - discountPriceByDollarAmount;
        }
        return priceAfterMarkup;
    }
}