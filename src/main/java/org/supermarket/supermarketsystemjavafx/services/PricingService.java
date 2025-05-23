package org.supermarket.supermarketsystemjavafx.services;

import org.supermarket.supermarketsystemjavafx.dao.PricingConfigDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.models.PricingConfig;
import org.supermarket.supermarketsystemjavafx.models.Product;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PricingService {
    private static PricingConfig pricingConfig;
    private static boolean initialized = false;

    // Initialize service with configuration
    private static synchronized void initialize() {
        if (!initialized) {
            try {
                pricingConfig = PricingConfigDAO.loadConfig();
            } catch (DatabaseException e) {
                // Fallback to default values if loading fails
                pricingConfig = new PricingConfig(0.30, 0.40, 7, 0.20);
                System.err.println("Failed to load pricing config, using defaults: " + e.getMessage());
            }
            initialized = true;
        }
    }

    /**
     * Gets the current pricing configuration
     */
    public static PricingConfig getPricingConfig() {
        if (!initialized) {
            initialize();
        }
        return pricingConfig;
    }

    /**
     * Updates the pricing configuration and persists it
     */
    public static void updatePricingConfig(PricingConfig newConfig) throws DatabaseException {
        if (newConfig == null) {
            throw new IllegalArgumentException("Pricing config cannot be null");
        }

        // Validate configuration
        validatePricingConfig(newConfig);

        pricingConfig = newConfig;
        PricingConfigDAO.saveConfig(newConfig);
    }

    /**
     * Validates pricing configuration values
     */
    private static void validatePricingConfig(PricingConfig config) {
        if (config.getFoodMarkupPercent() < 0 || config.getFoodMarkupPercent() > 5) {
            throw new IllegalArgumentException("Food markup must be between 0% and 500%");
        }
        if (config.getNonFoodMarkupPercent() < 0 || config.getNonFoodMarkupPercent() > 5) {
            throw new IllegalArgumentException("Non-food markup must be between 0% and 500%");
        }
        if (config.getExpirationThresholdDays() < 1 || config.getExpirationThresholdDays() > 365) {
            throw new IllegalArgumentException("Expiration threshold must be between 1 and 365 days");
        }
        if (config.getExpirationDiscountPercent() < 0 || config.getExpirationDiscountPercent() > 1) {
            throw new IllegalArgumentException("Expiration discount must be between 0% and 100%");
        }
    }

    /**
     * Calculates the selling price based on current pricing rules
     */
    public static double calculateSellingPrice(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (!initialized) {
            initialize();
        }

        double basePrice = product.getPurchasePrice();
        double markupPercent = getCategoryMarkup(product);
        double price = basePrice * (1 + markupPercent);

        if (shouldApplyExpirationDiscount(product)) {
            price *= (1 - pricingConfig.getExpirationDiscountPercent());
        }

        return roundToTwoDecimals(price);
    }

    /**
     * Gets the appropriate markup percentage for a product's category
     */
    private static double getCategoryMarkup(Product product) {
        return (product.getCategory() == Product.Category.FOOD)
                ? pricingConfig.getFoodMarkupPercent()
                : pricingConfig.getNonFoodMarkupPercent();
    }

    /**
     * Checks if expiration discount should be applied
     */
    public static boolean shouldApplyExpirationDiscount(Product product) {
        if (product.getExpirationDate() == null) {
            return false;
        }
        long daysUntilExpiration = getDaysUntilExpiration(product);
        return daysUntilExpiration < pricingConfig.getExpirationThresholdDays();
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
     * Gets days until expiration (returns Long.MAX_VALUE if no expiration date)
     */
    public static long getDaysUntilExpiration(Product product) {
        if (product.getExpirationDate() == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), product.getExpirationDate());
    }

    /**
     * Updates the selling price of a product based on current rules
     */
    public static void updateProductSellingPrice(Product product) {
        if (product != null) {
            product.setSellingPrice(calculateSellingPrice(product));
        }
    }

    /**
     * Recalculates selling prices for a list of products
     */
    public static void recalculateAllPrices(List<Product> products) {
        if (products != null) {
            products.forEach(PricingService::updateProductSellingPrice);
        }
    }

    /**
     * Calculates the total inventory value at purchase prices
     */
    public static double calculateInventoryValue(List<Product> products) {
        if (products == null) return 0;

        return products.stream()
                .filter(p -> !isExpired(p))
                .mapToDouble(p -> p.getPurchasePrice() * p.getQuantity())
                .sum();
    }

    /**
     * Calculates potential revenue if all products were sold
     */
    public static double calculatePotentialRevenue(List<Product> products) {
        if (products == null) return 0;

        return products.stream()
                .filter(p -> !isExpired(p))
                .mapToDouble(p -> calculateSellingPrice(p) * p.getQuantity())
                .sum();
    }

    /**
     * Calculates the gross profit margin for the inventory
     */
    public static double calculateGrossProfitMargin(List<Product> products) {
        double cost = calculateInventoryValue(products);
        double revenue = calculatePotentialRevenue(products);

        if (cost == 0) return 0; // Avoid division by zero

        return (revenue - cost) / revenue;
    }

    /**
     * Rounds a double value to 2 decimal places
     */
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Resets the service (primarily for testing)
     */
    static void reset() {
        initialized = false;
        pricingConfig = null;
    }
}