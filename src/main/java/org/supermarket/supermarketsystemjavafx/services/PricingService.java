package org.supermarket.supermarketsystemjavafx.services;

import org.supermarket.supermarketsystemjavafx.models.Product;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PricingService {
    // Configuration - could be loaded from properties/database
    private static final double FOOD_MARKUP_PERCENT = 0.30;  // 30% markup for food
    private static final double NON_FOOD_MARKUP_PERCENT = 0.40;  // 40% markup for non-food
    private static final int EXPIRATION_THRESHOLD_DAYS = 7;  // Apply discount if expires in <7 days
    private static final double EXPIRATION_DISCOUNT_PERCENT = 0.20;  // 20% discount for near-expiration

    /**
     * Calculates the selling price based on purchase price, category, and expiration date
     */
    public static double calculateSellingPrice(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Base price calculation with category markup
        double basePrice = product.getPurchasePrice();
        double markupPercent = (product.getCategory() == Product.Category.FOOD)
                ? FOOD_MARKUP_PERCENT
                : NON_FOOD_MARKUP_PERCENT;

        double price = basePrice * (1 + markupPercent);

        // Apply expiration discount if needed
        if (shouldApplyExpirationDiscount(product)) {
            price *= (1 - EXPIRATION_DISCOUNT_PERCENT);
        }

        // Round to 2 decimal places (cents)
        return Math.round(price * 100.0) / 100.0;
    }

    /**
     * Checks if a product should have expiration discount applied
     */
    public static boolean shouldApplyExpirationDiscount(Product product) {
        if (product.getExpirationDate() == null) {
            return false;
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), product.getExpirationDate());
        return daysUntilExpiration < EXPIRATION_THRESHOLD_DAYS;
    }

    /**
     * Checks if a product has expired
     */
    public static boolean isExpired(Product product) {
        if (product.getExpirationDate() == null) {
            return false;
        }
        return product.getExpirationDate().isBefore(LocalDate.now());
    }

    /**
     * Calculates the total value of current inventory
     */
    public static double calculateInventoryValue(List<Product> products) {
        return products.stream()
                .filter(p -> !isExpired(p))
                .mapToDouble(p -> p.getPurchasePrice() * p.getQuantity())
                .sum();
    }

    /**
     * Calculates the potential revenue if all products were sold
     */
    public static double calculatePotentialRevenue(List<Product> products) {
        return products.stream()
                .filter(p -> !isExpired(p))
                .mapToDouble(p -> calculateSellingPrice(p) * p.getQuantity())
                .sum();
    }

    /**
     * Updates the selling price of a product based on current rules
     */
    public static void updateProductSellingPrice(Product product) {
        if (product == null) return;
        product.setSellingPrice(calculateSellingPrice(product));
    }

    /**
     * Gets days until expiration (negative if expired)
     */
    public static long getDaysUntilExpiration(Product product) {
        if (product.getExpirationDate() == null) {
            return Long.MAX_VALUE; // Never expires
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), product.getExpirationDate());
    }
}