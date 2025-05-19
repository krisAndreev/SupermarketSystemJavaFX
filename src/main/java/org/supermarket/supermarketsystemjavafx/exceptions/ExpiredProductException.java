package org.supermarket.supermarketsystemjavafx.exceptions;

public class ExpiredProductException extends Exception {
    private final String productName;
    private final String expirationDate;

    public ExpiredProductException(String productName, String expirationDate) {
        super(String.format(
                "Product '%s' has expired (Expiration Date: %s)",
                productName, expirationDate
        ));
        this.productName = productName;
        this.expirationDate = expirationDate;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }
}