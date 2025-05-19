package org.supermarket.supermarketsystemjavafx.models;

import javafx.beans.property.*;

public class CartItem {
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final DoubleProperty unitPrice;
    private final IntegerProperty quantity;
    private final DoubleProperty totalPrice;

    public CartItem(int productId, String productName, double unitPrice, int quantity) {
        this.productId = new SimpleIntegerProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(unitPrice * quantity);
    }

    // Getters for properties
    public IntegerProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }

    // Regular getters
    public int getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotalPrice() { return totalPrice.get(); }

    // Setter for quantity (updates total price)
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        this.totalPrice.set(unitPrice.get() * quantity);
    }
}