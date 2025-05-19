package org.supermarket.supermarketsystemjavafx.exceptions;

public class InvalidPriceException extends Exception {
    private final double price;

    public InvalidPriceException(double price) {
        super(String.format("Invalid price value: %.2f", price));
        this.price = price;
    }

    // Getter
    public double getPrice() {
        return price;
    }
}