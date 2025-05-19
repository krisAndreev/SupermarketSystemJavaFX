package org.supermarket.supermarketsystemjavafx.models;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt {
    private final int saleId;
    private final int cashRegisterId;
    private final int employeeId;
    private final LocalDateTime saleDate;
    private final double totalAmount;
    private final List<ReceiptItem> items;

    public Receipt(int saleId, int cashRegisterId, int employeeId,
                   LocalDateTime saleDate, double totalAmount, List<ReceiptItem> items) {
        this.saleId = saleId;
        this.cashRegisterId = cashRegisterId;
        this.employeeId = employeeId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Supermarket Receipt ===\n");
        sb.append("Sale ID: ").append(saleId).append("\n");
        sb.append("Date: ").append(saleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Cash Register: ").append(cashRegisterId).append("\n");
        sb.append("Employee: ").append(employeeId).append("\n\n");
        sb.append("Items:\n");

        for (ReceiptItem item : items) {
            sb.append(String.format("%-20s %5d x %8.2f = %8.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));
        }

        sb.append("\nTotal Amount: ").append(String.format("%.2f", totalAmount)).append("\n");
        sb.append("===========================");

        return sb.toString();
    }
}