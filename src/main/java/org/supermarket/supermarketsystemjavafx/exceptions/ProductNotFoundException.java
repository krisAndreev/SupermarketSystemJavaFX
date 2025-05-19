package org.supermarket.supermarketsystemjavafx.exceptions;

public class ProductNotFoundException extends Exception {
    private final int productId;

    public ProductNotFoundException(int productId) {
        super(String.format("Product with ID %d not found", productId));
        this.productId = productId;
    }

    // Getter
    public int getProductId() {
        return productId;
    }
}