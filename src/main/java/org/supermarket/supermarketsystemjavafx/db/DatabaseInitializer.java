package org.supermarket.supermarketsystemjavafx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:sqlite:supermarket.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

            // Products table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "purchase_price REAL NOT NULL," +
                    "selling_price REAL NOT NULL," +
                    "category TEXT CHECK(category IN ('FOOD', 'NON_FOOD')) NOT NULL," +
                    "expiration_date TEXT NOT NULL," +  // Stored as YYYY-MM-DD
                    "quantity INTEGER NOT NULL," +
                    "barcode TEXT UNIQUE" +
                    ")");

            // Employees table
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "monthly_salary REAL NOT NULL," +
                    "pin_code TEXT" +  // For authentication
                    ")");

            // Cash registers table
            stmt.execute("CREATE TABLE IF NOT EXISTS cash_registers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "current_employee_id INTEGER REFERENCES employees(id)" +
                    ")");

            // Sales table
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cash_register_id INTEGER NOT NULL REFERENCES cash_registers(id)," +
                    "employee_id INTEGER NOT NULL REFERENCES employees(id)," +
                    "total_amount REAL NOT NULL," +
                    "sale_date TEXT NOT NULL," +  // ISO-8601 format
                    "payment_method TEXT" +  // CASH, CARD, etc.
                    ")");

            // Sale items (junction table)
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sale_id INTEGER NOT NULL REFERENCES sales(id) ON DELETE CASCADE," +
                    "product_id INTEGER NOT NULL REFERENCES products(id)," +
                    "quantity INTEGER NOT NULL," +
                    "unit_price REAL NOT NULL," +
                    "total_price REAL NOT NULL" +
                    ")");

            // Configuration table (for store settings)
            stmt.execute("CREATE TABLE IF NOT EXISTS store_config (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "food_markup_percent REAL DEFAULT 0.3," +
                    "non_food_markup_percent REAL DEFAULT 0.4," +
                    "expiration_threshold_days INTEGER DEFAULT 7," +
                    "expiration_discount_percent REAL DEFAULT 0.2" +
                    ")");

            // Insert default configuration if empty
            stmt.execute("INSERT OR IGNORE INTO store_config(id) VALUES(1)");

            System.out.println("Database initialized successfully");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static void migrateExistingDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Check if old products table exists (without expiration date)
            boolean needsMigration = false;
            try {
                stmt.executeQuery("SELECT expiration_date FROM products LIMIT 1");
            } catch (SQLException e) {
                needsMigration = true;
            }

            if (needsMigration) {
                System.out.println("Migrating existing database...");

                // Add new columns to existing tables
                String[] migrationQueries = {
                        "ALTER TABLE products ADD COLUMN purchase_price REAL DEFAULT 0",
                        "ALTER TABLE products ADD COLUMN selling_price REAL DEFAULT 0",
                        "ALTER TABLE products ADD COLUMN expiration_date TEXT DEFAULT '2023-12-31'",
                        "ALTER TABLE products ADD COLUMN category TEXT DEFAULT 'NON_FOOD'",
                        "UPDATE products SET purchase_price = price * 0.7", // Estimate cost price
                        "UPDATE products SET selling_price = price",        // Keep existing price
                        "ALTER TABLE products RENAME COLUMN price TO old_price" // Optional: keep old price
                };

                for (String query : migrationQueries) {
                    try {
                        stmt.execute(query);
                    } catch (SQLException e) {
                        System.out.println("Migration step skipped: " + e.getMessage());
                    }
                }

                // Create new tables if they don't exist
                initialize();
            }

        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }
}