package org.supermarket.supermarketsystemjavafx.models;

import java.time.LocalDateTime;

public class Sale {
    private final int id;
    private final int cashRegisterId;
    private final int employeeId;
    private final double totalAmount;
    private final LocalDateTime saleDate;

    public Sale(int id, int cashRegisterId, int employeeId, double totalAmount, LocalDateTime saleDate) {
        this.id = id;
        this.cashRegisterId = cashRegisterId;
        this.employeeId = employeeId;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    // Getters
    public int getId() { return id; }
    public int getCashRegisterId() { return cashRegisterId; }
    public int getEmployeeId() { return employeeId; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getSaleDate() { return saleDate; }
}