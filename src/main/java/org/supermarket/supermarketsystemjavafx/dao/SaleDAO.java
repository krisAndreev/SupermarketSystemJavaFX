package org.supermarket.supermarketsystemjavafx.dao;

import org.supermarket.supermarketsystemjavafx.db.DatabaseConnection;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Product;
import org.supermarket.supermarketsystemjavafx.models.Sale;
import org.supermarket.supermarketsystemjavafx.exceptions.InsufficientStockException;
import org.supermarket.supermarketsystemjavafx.models.SaleItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {
    private static final String DB_URL = "jdbc:sqlite:supermarket.db";

    public static Sale createSale(int cashRegisterId, int employeeId, double totalAmount, LocalDateTime saleDate)
            throws SQLException {
        String sql = "INSERT INTO sales(cash_register_id, employee_id, total_amount, sale_date) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cashRegisterId);
            pstmt.setInt(2, employeeId);
            pstmt.setDouble(3, totalAmount);
            // Store date in consistent format
            pstmt.setString(4, saleDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Sale(
                            generatedKeys.getInt(1),
                            cashRegisterId,
                            employeeId,
                            totalAmount,
                            saleDate
                    );
                } else {
                    throw new SQLException("Creating sale failed, no ID obtained.");
                }
            }
        }
    }

    public static void addSaleItem(int saleId, int productId, int quantity, double unitPrice)
            throws SQLException, InsufficientStockException, ProductNotFoundException, DatabaseException {
        // First check stock
        Product product = ProductDAO.getProductById(productId);
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getName() +
                            ". Available: " + product.getQuantity() +
                            ", Requested: " + quantity
            );
        }

        // Calculate total price for this line item
        double totalPrice = quantity * unitPrice;

        String sql = "INSERT INTO sale_items(sale_id, product_id, quantity, unit_price, total_price) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.setDouble(5, totalPrice);  // Added total price parameter

            pstmt.executeUpdate();

            // Update product stock after successful sale
            ProductDAO.updateProductQuantity(productId, product.getQuantity() - quantity);
        }
    }

    public static Sale getSaleById(int saleId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Handle timestamp parsing more robustly
                String timestampStr = rs.getString("sale_date");
                LocalDateTime saleDate;

                try {
                    // Try ISO format first (what Java 8+ uses by default)
                    saleDate = LocalDateTime.parse(timestampStr);
                } catch (DateTimeParseException e1) {
                    try {
                        // Try SQLite's default format if ISO fails
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        saleDate = LocalDateTime.parse(timestampStr, formatter);
                    } catch (DateTimeParseException e2) {
                        // Fallback to current time if parsing fails
                        saleDate = LocalDateTime.now();
                    }
                }

                return new Sale(
                        rs.getInt("id"),
                        rs.getInt("cash_register_id"),
                        rs.getInt("employee_id"),
                        rs.getDouble("total_amount"),
                        saleDate
                );
            }
        }
        return null;
    }

    public static List<SaleItem> getSaleItems(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.name as product_name FROM sale_items si " +
                "JOIN products p ON si.product_id = p.id WHERE si.sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(new SaleItem(
                        rs.getInt("id"),
                        rs.getInt("sale_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total_price")  // Make sure your SaleItem model has this field
                ));
            }
        }
        return items;
    }
}