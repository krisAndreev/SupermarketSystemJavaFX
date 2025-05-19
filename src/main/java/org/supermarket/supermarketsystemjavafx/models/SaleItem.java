package org.supermarket.supermarketsystemjavafx.models;

public class SaleItem {
    private final int id;
    private final int saleId;
    private final int productId;
    private final int quantity;
    private final double unitPrice;

    public SaleItem(int id, int saleId, int productId, int quantity, double unitPrice) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
}