package io.council;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order10 {

    public enum Status { PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED }

    public static class LineItem {
        private final String productId;
        private final String productName;
        private int quantity;
        private final double unitPrice;

        public LineItem(String productId, String productName, int quantity, double unitPrice) {
            if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
            if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative");
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public double getSubtotal() { return quantity * unitPrice; }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }

        @Override
        public String toString() {
            return String.format("%s x%d @ %.2f", productName, quantity, unitPrice);
        }
    }

    private final String orderId;
    private final String customerId;
    private final List<LineItem> items;
    private Status status;
    private double discountPercent;
    private double taxRate;
    private String trackingNumber;

    public Order10(String orderId, String customerId) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Order ID required");
        if (customerId == null || customerId.isBlank()) throw new IllegalArgumentException("Customer ID required");
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
        this.discountPercent = 0.0;
        this.taxRate = 0.0;
    }

    public void addItem(String productId, String productName, int quantity, double unitPrice) {
        if (status != Status.PENDING) throw new IllegalStateException("Cannot modify order after confirmation");
        for (LineItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.quantity += quantity;
                return;
            }
        }
        items.add(new LineItem(productId, productName, quantity, unitPrice));
    }

    public boolean removeItem(String productId) {
        if (status != Status.PENDING) throw new IllegalStateException("Cannot modify order after confirmation");
        return items.removeIf(item -> item.getProductId().equals(productId));
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(LineItem::getSubtotal).sum();
    }

    public double getDiscountAmount() {
        return getSubtotal() * discountPercent / 100.0;
    }

    public double getTaxAmount() {
        double afterDiscount = getSubtotal() - getDiscountAmount();
        return afterDiscount * taxRate / 100.0;
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount() + getTaxAmount();
    }

    public void applyDiscount(double percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Discount must be 0-100");
        if (status != Status.PENDING && status != Status.CONFIRMED)
            throw new IllegalStateException("Cannot apply discount to order in state: " + status);
        this.discountPercent = percent;
    }

    public void setTaxRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Tax rate cannot be negative");
        this.taxRate = rate;
    }

    public void confirm() {
        if (status != Status.PENDING) throw new IllegalStateException("Order must be pending to confirm");
        if (items.isEmpty()) throw new IllegalStateException("Cannot confirm empty order");
        status = Status.CONFIRMED;
    }

    public void process() {
        if (status != Status.CONFIRMED) throw new IllegalStateException("Order must be confirmed to process");
        status = Status.PROCESSING;
    }

    public void ship(String trackingNumber) {
        if (status != Status.PROCESSING) throw new IllegalStateException("Order must be processing to ship");
        if (trackingNumber == null || trackingNumber.isBlank())
            throw new IllegalArgumentException("Tracking number required");
        this.trackingNumber = trackingNumber;
        status = Status.SHIPPED;
    }

    public void deliver() {
        if (status != Status.SHIPPED) throw new IllegalStateException("Order must be shipped to deliver");
        status = Status.DELIVERED;
    }

    public void cancel() {
        if (status == Status.SHIPPED || status == Status.DELIVERED)
            throw new IllegalStateException("Cannot cancel shipped or delivered order");
        if (status == Status.CANCELLED) throw new IllegalStateException("Order already cancelled");
        status = Status.CANCELLED;
    }

    public void refund() {
        if (status != Status.DELIVERED) throw new IllegalStateException("Can only refund delivered orders");
        status = Status.REFUNDED;
    }

    public boolean isCancellable() {
        return status == Status.PENDING || status == Status.CONFIRMED || status == Status.PROCESSING;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<LineItem> getItems() { return Collections.unmodifiableList(items); }
    public Status getStatus() { return status; }
    public double getDiscountPercent() { return discountPercent; }
    public double getTaxRate() { return taxRate; }
    public String getTrackingNumber() { return trackingNumber; }
    public int getItemCount() { return items.size(); }

    @Override
    public String toString() {
        return String.format("Order10[%s, customer=%s, items=%d, total=%.2f, status=%s]",
                orderId, customerId, items.size(), getTotal(), status);
    }
}
