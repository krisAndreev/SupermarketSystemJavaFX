package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import org.supermarket.supermarketsystemjavafx.dao.ProductDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.models.Product;
import org.supermarket.supermarketsystemjavafx.services.PricingService;

import java.net.URL;
import java.sql.*;
import java.util.List;
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
    private Button setupButton;

    @FXML
    private Tab employeesTab;

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

    private void loadProductsFromDatabase() {
        try {
            List<Product> productList = ProductDAO.getAllProducts();
            products.clear();

            // Apply pricing logic and filter out expired products
            for (Product product : productList) {
                if (!PricingService.isExpired(product)) {
                    // Ensure selling price is calculated
                    PricingService.updateProductSellingPrice(product);
                    products.add(product);
                }
            }

            productsTable.setItems(products);

        } catch (DatabaseException e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            // Show user-friendly alert
            //throw("Database Error", "Failed to load products. Please try again.");
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

    @FXML
    private void setupButtonClicked() throws IOException
    {
        // Load the sales view
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SetupWindow.fxml")
        );
        Parent root = loader.load();

        // Create a new stage for the sales window
        Stage salesStage = new Stage();
        salesStage.setTitle("Sales Management");
        salesStage.setScene(new Scene(root));

        // Set the owner to maintain window relationship
        salesStage.initOwner(setupButton.getScene().getWindow());

        // Optional: Set modality if you want to block interaction with main window
        // salesStage.initModality(Modality.WINDOW_MODAL);

        salesStage.show();

        // Optional: Center the new window relative to main window
        salesStage.centerOnScreen();
    }

    @FXML
    private void addButtonClicked() throws IOException
    {
        FXMLLoader loader;
        // Load the sales view
        if(inventoryTab.isSelected())
        {
             loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddItem.fxml")
            );
        }
        else if(employeesTab.isSelected())
        {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddEmployee.fxml")
            );
        }
        else
        {
            loader = new FXMLLoader();
        }
        Parent root = loader.load();

        // Create a new stage for the sales window
        Stage salesStage = new Stage();
        salesStage.setTitle("Add");
        salesStage.setScene(new Scene(root));

        // Set the owner to maintain window relationship
        salesStage.initOwner(setupButton.getScene().getWindow());

        // Optional: Set modality if you want to block interaction with main window
        // salesStage.initModality(Modality.WINDOW_MODAL);

        salesStage.show();

        // Optional: Center the new window relative to main window
        salesStage.centerOnScreen();
    }
}