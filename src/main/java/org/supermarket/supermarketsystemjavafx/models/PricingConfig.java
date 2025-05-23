package org.supermarket.supermarketsystemjavafx.models;

public class PricingConfig {
    private double foodMarkupPercent;
    private double nonFoodMarkupPercent;
    private int expirationThresholdDays;
    private double expirationDiscountPercent;

    // Constructor
    public PricingConfig(double foodMarkupPercent, double nonFoodMarkupPercent,
                         int expirationThresholdDays, double expirationDiscountPercent) {
        this.foodMarkupPercent = foodMarkupPercent;
        this.nonFoodMarkupPercent = nonFoodMarkupPercent;
        this.expirationThresholdDays = expirationThresholdDays;
        this.expirationDiscountPercent = expirationDiscountPercent;
    }

    // Getters and Setters
    public double getFoodMarkupPercent() {
        return foodMarkupPercent;
    }

    public void setFoodMarkupPercent(double foodMarkupPercent) {
        this.foodMarkupPercent = foodMarkupPercent;
    }

    public double getNonFoodMarkupPercent() {
        return nonFoodMarkupPercent;
    }

    public void setNonFoodMarkupPercent(double nonFoodMarkupPercent) {
        this.nonFoodMarkupPercent = nonFoodMarkupPercent;
    }

    public int getExpirationThresholdDays() {
        return expirationThresholdDays;
    }

    public void setExpirationThresholdDays(int expirationThresholdDays) {
        this.expirationThresholdDays = expirationThresholdDays;
    }

    public double getExpirationDiscountPercent() {
        return expirationDiscountPercent;
    }

    public void setExpirationDiscountPercent(double expirationDiscountPercent) {
        this.expirationDiscountPercent = expirationDiscountPercent;
    }
}