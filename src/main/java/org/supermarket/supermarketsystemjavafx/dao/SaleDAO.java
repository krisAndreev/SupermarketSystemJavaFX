package org.supermarket.supermarketsystemjavafx.dao;

import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Product;
import org.supermarket.supermarketsystemjavafx.models.Sale;
import org.supermarket.supermarketsystemjavafx.exceptions.InsufficientStockException;

import java.sql.*;
import java.time.LocalDateTime;
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
            pstmt.setString(4, saleDate.toString());

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

        String sql = "INSERT INTO sale_items(sale_id, product_id, quantity, unit_price) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);

            pstmt.executeUpdate();
        }
    }
}