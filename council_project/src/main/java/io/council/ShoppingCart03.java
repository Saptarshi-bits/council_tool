package io.council;

import java.util.*;

public class ShoppingCart03 {

    public static class Item {
        private final String id;
        private final String name;
        private final double unitPrice;
        private int quantity;

        public Item(String id, String name, double unitPrice, int quantity) {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Item ID required");
            if (unitPrice < 0) throw new IllegalArgumentException("Price cannot be negative");
            if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
            this.id = id;
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return unitPrice * quantity; }
        public void setQuantity(int q) {
            if (q < 1) throw new IllegalArgumentException("Quantity must be at least 1");
            this.quantity = q;
        }
    }

    private final Map<String, Item> items = new LinkedHashMap<>();
    private String couponCode;
    private double flatDiscount;

    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (items.containsKey(item.getId())) {
            items.get(item.getId()).setQuantity(items.get(item.getId()).getQuantity() + item.getQuantity());
        } else {
            items.put(item.getId(), item);
        }
    }

    public void removeItem(String itemId) {
        if (!items.containsKey(itemId)) throw new NoSuchElementException("Item not in cart: " + itemId);
        items.remove(itemId);
    }

    public void updateQuantity(String itemId, int newQuantity) {
        if (!items.containsKey(itemId)) throw new NoSuchElementException("Item not in cart: " + itemId);
        items.get(itemId).setQuantity(newQuantity);
    }

    public void applyCoupon(String code, double discountAmount) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Invalid coupon");
        if (discountAmount <= 0) throw new IllegalArgumentException("Discount must be positive");
        this.couponCode = code;
        this.flatDiscount = discountAmount;
    }

    public void removeCoupon() {
        this.couponCode = null;
        this.flatDiscount = 0;
    }

    public double getSubtotal() {
        return items.values().stream().mapToDouble(Item::getSubtotal).sum();
    }

    public double getDiscount() {
        return couponCode != null ? Math.min(flatDiscount, getSubtotal()) : 0;
    }

    public double getTax(double taxRate) {
        if (taxRate < 0) throw new IllegalArgumentException("Tax rate cannot be negative");
        return (getSubtotal() - getDiscount()) * taxRate;
    }

    public double getTotal(double taxRate) {
        return getSubtotal() - getDiscount() + getTax(taxRate);
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(Item::getQuantity).sum();
    }

    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); removeCoupon(); }
    public Collection<Item> getItems() { return Collections.unmodifiableCollection(items.values()); }
    public String getCouponCode() { return couponCode; }

    public Optional<Item> getMostExpensiveItem() {
        return items.values().stream().max(Comparator.comparingDouble(Item::getUnitPrice));
    }
}
