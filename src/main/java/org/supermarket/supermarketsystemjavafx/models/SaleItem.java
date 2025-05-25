package org.supermarket.supermarketsystemjavafx.models;

public class SaleItem {
    private final int id;
    private final int saleId;
    private final int productId;
    private final String productName;
    private final int quantity;
    private final double unitPrice;

    public SaleItem(int id, int saleId, int productId, String productName, int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
}