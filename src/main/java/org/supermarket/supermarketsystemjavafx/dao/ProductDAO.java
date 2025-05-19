package org.supermarket.supermarketsystemjavafx.dao;

import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Product;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final String DB_URL = "jdbc:sqlite:supermarket.db";

    // SQL Statements
    private static final String GET_ALL_PRODUCTS = "SELECT * FROM products";
    private static final String GET_PRODUCT_BY_ID = "SELECT * FROM products WHERE id = ?";
    private static final String GET_PRODUCT_BY_BARCODE = "SELECT * FROM products WHERE barcode = ?";
    private static final String INSERT_PRODUCT = """
        INSERT INTO products(name, purchase_price, selling_price, category, 
        expiration_date, quantity, barcode) 
        VALUES(?,?,?,?,?,?,?)""";
    private static final String UPDATE_PRODUCT = """
        UPDATE products SET name = ?, purchase_price = ?, selling_price = ?, 
        category = ?, expiration_date = ?, quantity = ?, barcode = ? 
        WHERE id = ?""";
    private static final String UPDATE_PRODUCT_QUANTITY = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
    private static final String DELETE_PRODUCT = "DELETE FROM products WHERE id = ?";
    private static final String GET_EXPIRING_PRODUCTS = """
        SELECT * FROM products 
        WHERE expiration_date BETWEEN date('now') AND date('now', '+7 days')""";

    /**
     * Retrieves all products from the database
     */
    public static List<Product> getAllProducts() throws DatabaseException {
        List<Product> products = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_PRODUCTS)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;

        } catch (SQLException e) {
            throw new DatabaseException("Get all products", e);
        }
    }

    /**
     * Gets a product by its ID
     */
    public static Product getProductById(int id) throws ProductNotFoundException, DatabaseException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(GET_PRODUCT_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            } else {
                throw new ProductNotFoundException(id);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Get product by ID", e);
        }
    }

    /**
     * Gets a product by barcode
     */
    public static Product getProductByBarcode(String barcode) throws ProductNotFoundException, DatabaseException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(GET_PRODUCT_BY_BARCODE)) {

            pstmt.setString(1, barcode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            } else {
                throw new ProductNotFoundException(Integer.parseInt(barcode));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Get product by barcode", e);
        }
    }

    /**
     * Adds a new product to the database
     */
    public static void addProduct(Product product) throws DatabaseException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_PRODUCT, Statement.RETURN_GENERATED_KEYS)) {

            setProductStatementParameters(pstmt, product);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("Add product", "No rows affected");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Add product", e);
        }
    }

    /**
     * Updates an existing product
     */
    public static void updateProduct(Product product) throws DatabaseException, ProductNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PRODUCT)) {

            setProductStatementParameters(pstmt, product);
            pstmt.setInt(8, product.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ProductNotFoundException(product.getId());
            }

        } catch (SQLException e) {
            throw new DatabaseException("Update product", e);
        }
    }

    /**
     * Updates product quantity (adds or subtracts from current quantity)
     */
    public static void updateProductQuantity(int productId, int quantityChange)
            throws DatabaseException, ProductNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PRODUCT_QUANTITY)) {

            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, productId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ProductNotFoundException(productId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Update product quantity", e);
        }
    }

    /**
     * Deletes a product from the database
     */
    public static void deleteProduct(int productId) throws DatabaseException, ProductNotFoundException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_PRODUCT)) {

            pstmt.setInt(1, productId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ProductNotFoundException(productId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Delete product", e);
        }
    }

    /**
     * Gets products that are expiring soon (within 7 days)
     */
    public static List<Product> getExpiringProducts() throws DatabaseException {
        List<Product> products = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_EXPIRING_PRODUCTS)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;

        } catch (SQLException e) {
            throw new DatabaseException("Get expiring products", e);
        }
    }

    // Helper method to map ResultSet to Product object
    private static Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("purchase_price"),
                rs.getDouble("selling_price"),
                Product.Category.valueOf(rs.getString("category")),
                LocalDate.parse(rs.getString("expiration_date")),
                rs.getInt("quantity"),
                rs.getString("barcode")
        );
    }

    // Helper method to set PreparedStatement parameters for Product
    private static void setProductStatementParameters(PreparedStatement pstmt, Product product)
            throws SQLException {
        pstmt.setString(1, product.getName());
        pstmt.setDouble(2, product.getPurchasePrice());
        pstmt.setDouble(3, product.getSellingPrice());
        pstmt.setString(4, product.getCategory().name());
        pstmt.setString(5, product.getExpirationDate().toString());
        pstmt.setInt(6, product.getQuantity());
        pstmt.setString(7, product.getBarcode());
    }
}