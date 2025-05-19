package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.*;
import org.supermarket.supermarketsystemjavafx.dao.ProductDAO;
import org.supermarket.supermarketsystemjavafx.dao.SaleDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.InsufficientStockException;
import org.supermarket.supermarketsystemjavafx.services.PricingService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SalesController {

    // FXML Components
    @FXML private TextField productIdField;
    @FXML private Label productNameLabel;
    @FXML private TextField quantityField;
    @FXML private Button addButton;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, Integer> productIdColumn;
    @FXML private TableColumn<CartItem, String> productNameColumn;
    @FXML private TableColumn<CartItem, Double> unitPriceColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> totalPriceColumn;
    @FXML private TableColumn<CartItem, Void> actionColumn;
    @FXML private Label totalItemsLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Button clearButton;
    @FXML private Button finalizeButton;

    // Data
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double grandTotal = 0.0;
    private int totalItems = 0;
    private int currentCashRegisterId = 1; // Default cash register
    private int currentEmployeeId = 1; // Default employee

    public void initialize() {
        // Configure table columns
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Add remove button to action column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });

        cartTable.setItems(cartItems);

        // Add listener to product ID field
        productIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int productId = Integer.parseInt(newValue);
                    Product product = ProductDAO.getProductById(productId);
                    if (product != null) {
                        productNameLabel.setText(product.getName());
                        productNameLabel.setStyle("-fx-text-fill: green;");
                    } else {
                        productNameLabel.setText("Product not found");
                        productNameLabel.setStyle("-fx-text-fill: red;");
                    }
                } catch (NumberFormatException | ProductNotFoundException | DatabaseException e) {
                    productNameLabel.setText("Invalid ID");
                    productNameLabel.setStyle("-fx-text-fill: orange;");
                }
            } else {
                productNameLabel.setText("");
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        try {
            int productId = Integer.parseInt(productIdField.getText());
            int quantity = Integer.parseInt(quantityField.getText());

            if (quantity <= 0) {
                showAlert("Invalid Quantity", "Quantity must be greater than 0");
                return;
            }

            Product product = ProductDAO.getProductById(productId);
            if (product == null) {
                showAlert("Product Not Found", "No product found with ID: " + productId);
                return;
            }

            if (PricingService.isExpired(product)) {
                showAlert("Expired Product", "Cannot sell expired product: " + product.getName());
                return;
            }

            // Check if product already in cart
            for (CartItem item : cartItems) {
                if (item.getProductId() == productId) {
                    item.setQuantity(item.getQuantity() + quantity);
                    updateTotals();
                    cartTable.refresh();
                    clearInputFields();
                    return;
                }
            }

            // Add new item to cart
            CartItem newItem = new CartItem(
                    productId,
                    product.getName(),
                    product.getSellingPrice(),
                    quantity
            );

            cartItems.add(newItem);
            updateTotals();
            clearInputFields();

        } catch (NumberFormatException | ProductNotFoundException | DatabaseException e) {
            showAlert("Invalid Input", "Please enter valid numbers for ID and quantity");
        }
    }

    private void removeFromCart(CartItem item) {
        cartItems.remove(item);
        updateTotals();
    }

    private void updateTotals() {
        totalItems = 0;
        grandTotal = 0.0;

        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            grandTotal += item.getTotalPrice();
        }

        totalItemsLabel.setText(String.valueOf(totalItems));
        grandTotalLabel.setText(String.format("%.2f", grandTotal));
    }

    @FXML
    private void handleClearCart() {
        cartItems.clear();
        updateTotals();
    }

    @FXML
    private void handleFinalizeOrder() {
        if (cartItems.isEmpty()) {
            showAlert("Empty Cart", "Please add products to the cart before finalizing");
            return;
        }

        try {
            // 1. Validate stock availability
            Map<Integer, Integer> productQuantities = new HashMap<>();
            for (CartItem item : cartItems) {
                productQuantities.put(item.getProductId(), item.getQuantity());
            }

            // 2. Process sale in database
            Sale sale = SaleDAO.createSale(
                    currentCashRegisterId,
                    currentEmployeeId,
                    grandTotal,
                    LocalDateTime.now()
            );

            // 3. Add sale items and update inventory
            for (CartItem item : cartItems) {
                SaleDAO.addSaleItem(
                        sale.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                );

                // Update product quantity in database
                ProductDAO.updateProductQuantity(
                        item.getProductId(),
                        -item.getQuantity() // Subtract from inventory
                );
            }

            // 4. Generate receipt
            generateReceipt(sale);

            // 5. Clear cart
            handleClearCart();

        } catch (InsufficientStockException e) {
            showAlert("Insufficient Stock", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to process sale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateReceipt(Sale sale) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/supermarket/supermarketsystemjavafx/views/Receipt.fxml"));
        Parent root = loader.load();

        ReceiptController controller = loader.getController();
        controller.setSaleData(sale, cartItems);

        Stage stage = new Stage();
        stage.setTitle("Receipt - Sale #" + sale.getId());
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void clearInputFields() {
        productIdField.clear();
        quantityField.clear();
        productNameLabel.setText("");
        productIdField.requestFocus();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Additional methods to set cash register and employee
    public void setCashRegisterId(int cashRegisterId) {
        this.currentCashRegisterId = cashRegisterId;
    }

    public void setEmployeeId(int employeeId) {
        this.currentEmployeeId = employeeId;
    }
}