package org.supermarket.supermarketsystemjavafx.models;

public class ReceiptItem {
    private final String productName;
    private final int quantity;
    private final double unitPrice;
    private final double totalPrice;

    public ReceiptItem(String productName, int quantity, double unitPrice, double totalPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
}