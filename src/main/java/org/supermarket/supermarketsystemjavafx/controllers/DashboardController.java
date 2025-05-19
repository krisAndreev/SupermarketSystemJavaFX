package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.supermarket.supermarketsystemjavafx.models.Product;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
;

public class DashboardController implements Initializable
{
    @FXML
    private TableView<Product> productsTable;

    @FXML
    private TableColumn<Product, Integer> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, String> barcodeColumn;

    private ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    private Button salesButton;

    @FXML
    private Tab inventoryTab;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Set up the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        // Load data from database
        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase()
    {
        String sql = "SELECT * FROM products";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:supermarket.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            products.clear();

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getString("barcode")
                ));
            }

            productsTable.setItems(products);

        } catch (SQLException e) {
            System.err.println("Error loading products from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sellButtonClicked() throws IOException
    {
        // Load the sales view
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SalesWindow.fxml")
        );
        Parent root = loader.load();

        // Create a new stage for the sales window
        Stage salesStage = new Stage();
        salesStage.setTitle("Sales Management");
        salesStage.setScene(new Scene(root));

        // Set the owner to maintain window relationship
        salesStage.initOwner(salesButton.getScene().getWindow());

        // Optional: Set modality if you want to block interaction with main window
        // salesStage.initModality(Modality.WINDOW_MODAL);

        salesStage.show();

        // Optional: Center the new window relative to main window
        salesStage.centerOnScreen();
    }
}