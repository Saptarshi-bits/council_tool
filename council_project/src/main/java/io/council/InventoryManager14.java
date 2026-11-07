package io.council;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager14 {

    public static class Product {
        private final String id;
        private final String name;
        private int stock;
        private int reorderThreshold;
        private int reorderQuantity;

        public Product(String id, String name, int initialStock, int reorderThreshold, int reorderQuantity) {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Product ID required");
            if (initialStock < 0) throw new IllegalArgumentException("Stock cannot be negative");
            if (reorderThreshold < 0) throw new IllegalArgumentException("Reorder threshold cannot be negative");
            if (reorderQuantity < 1) throw new IllegalArgumentException("Reorder quantity must be positive");
            this.id = id;
            this.name = name;
            this.stock = initialStock;
            this.reorderThreshold = reorderThreshold;
            this.reorderQuantity = reorderQuantity;
        }

        public boolean needsReorder() { return stock <= reorderThreshold; }
        public String getId() { return id; }
        public String getName() { return name; }
        public int getStock() { return stock; }
        public int getReorderThreshold() { return reorderThreshold; }
        public int getReorderQuantity() { return reorderQuantity; }

        @Override
        public String toString() {
            return String.format("Product[%s, %s, stock=%d]", id, name, stock);
        }
    }

    private final Map<String, Product> catalog;

    public InventoryManager14() {
        this.catalog = new HashMap<>();
    }

    public void addProduct(Product product) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        catalog.put(product.getId(), product);
    }

    public void removeProduct(String productId) {
        if (!catalog.containsKey(productId)) throw new IllegalArgumentException("Product not found: " + productId);
        catalog.remove(productId);
    }

    public void restock(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Restock quantity must be positive");
        Product p = getOrThrow(productId);
        p.stock += quantity;
    }

    public boolean fulfillOrder(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Product p = getOrThrow(productId);
        if (p.stock < quantity) return false;
        p.stock -= quantity;
        return true;
    }

    public boolean transfer(String fromProductId, String toProductId, int quantity) {
        if (fromProductId.equals(toProductId)) throw new IllegalArgumentException("Cannot transfer to same product");
        if (quantity <= 0) throw new IllegalArgumentException("Transfer quantity must be positive");
        Product from = getOrThrow(fromProductId);
        Product to = getOrThrow(toProductId);
        if (from.stock < quantity) return false;
        from.stock -= quantity;
        to.stock += quantity;
        return true;
    }

    public void adjustStock(String productId, int delta) {
        Product p = getOrThrow(productId);
        if (p.stock + delta < 0) throw new IllegalArgumentException("Adjustment would result in negative stock");
        p.stock += delta;
    }

    public int getStock(String productId) {
        return getOrThrow(productId).stock;
    }

    public boolean isLowStock(String productId) {
        return getOrThrow(productId).needsReorder();
    }

    public Map<String, Integer> getLowStockProducts() {
        Map<String, Integer> low = new HashMap<>();
        for (Product p : catalog.values()) {
            if (p.needsReorder()) low.put(p.getId(), p.stock);
        }
        return low;
    }

    public int getTotalStockValue(Map<String, Double> prices) {
        int total = 0;
        for (Product p : catalog.values()) {
            Double price = prices.get(p.getId());
            if (price != null) total += (int)(p.stock * price);
        }
        return total;
    }

    public int getProductCount() { return catalog.size(); }

    private Product getOrThrow(String productId) {
        Product p = catalog.get(productId);
        if (p == null) throw new IllegalArgumentException("Product not found: " + productId);
        return p;
    }
}
