package org.supermarket.supermarketsystemjavafx.exceptions;

public class InsufficientStockException extends Exception {
    private final String productName;
    private final int availableQuantity;
    private final int requestedQuantity;

    public InsufficientStockException(String productName, int availableQuantity, int requestedQuantity) {
        super(String.format(
                "Insufficient stock for product '%s'. Available: %d, Requested: %d",
                productName, availableQuantity, requestedQuantity
        ));
        this.productName = productName;
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }

    // Additional constructors for different use cases
    public InsufficientStockException(String message) {
        super(message);
        this.productName = "";
        this.availableQuantity = 0;
        this.requestedQuantity = 0;
    }

    public InsufficientStockException(String productName, String message) {
        super(message);
        this.productName = productName;
        this.availableQuantity = 0;
        this.requestedQuantity = 0;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}