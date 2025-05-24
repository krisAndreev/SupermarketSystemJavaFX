package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.supermarket.supermarketsystemjavafx.dao.ProductDAO;
import org.supermarket.supermarketsystemjavafx.dao.SaleDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.InsufficientStockException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.CartItem;
import org.supermarket.supermarketsystemjavafx.models.Employee;
import org.supermarket.supermarketsystemjavafx.models.Product;
import org.supermarket.supermarketsystemjavafx.models.Sale;
import org.supermarket.supermarketsystemjavafx.services.PricingService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
    @FXML private Label employeeInfoLabel;
    @FXML private Label cashRegisterLabel;
    @FXML private TextField manualEmployeeIdField;
    @FXML private HBox manualEntryBox;

    // Data
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double grandTotal = 0.0;
    private int totalItems = 0;
    private int currentCashRegisterId = 1;
    private int currentEmployeeId = 1;
    private Employee currentEmployee;

    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
        updateEmployeeDisplay();
    }

    @FXML
    public void initialize() {
        this.currentCashRegisterId = generateCashRegisterId();
        cashRegisterLabel.setText("Cash Register: #" + currentCashRegisterId);

        // Hide manual entry by default
        manualEntryBox.setVisible(false);

        configureTableColumns();
        setupRemoveButtons();
        setupProductIdListener();
    }

    private int generateCashRegisterId() {
        // Simple implementation - could be from config or database
        return (int) (Math.random() * 1000) + 1;
    }

    private void updateEmployeeDisplay() {
        if (currentEmployee != null) {
            employeeInfoLabel.setText("Employee: " + currentEmployee.getName() +
                    " (ID: " + currentEmployee.getId() + ")");
            manualEntryBox.setVisible(false);
        } else {
            employeeInfoLabel.setText("No employee selected");
            manualEntryBox.setVisible(true);
        }
    }

    @FXML
    private void handleManualEmployeeSubmit() {
        try {
            int employeeId = Integer.parseInt(manualEmployeeIdField.getText());
            // In a real app, you would fetch the employee from database here
            currentEmployee = new Employee(employeeId, "Manual Entry", 0);
            updateEmployeeDisplay();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid employee ID");
        }
    }

    private void configureTableColumns() {
        // Solution using lambda expressions instead of PropertyValueFactory
        productIdColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getProductId()).asObject());

        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());

        quantityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        totalPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());

        cartTable.setItems(cartItems);
    }

    private void setupRemoveButtons() {
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
    }

    private void setupProductIdListener() {
        productIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int productId = Integer.parseInt(newValue);
                    Product product = ProductDAO.getProductById(productId);
                    updateProductLabel(product);
                } catch (NumberFormatException | ProductNotFoundException | DatabaseException e) {
                    productNameLabel.setText("Invalid ID");
                    productNameLabel.setStyle("-fx-text-fill: orange;");
                }
            } else {
                productNameLabel.setText("");
            }
        });
    }

    private void updateProductLabel(Product product) {
        if (product != null) {
            productNameLabel.setText(product.getName());
            productNameLabel.setStyle("-fx-text-fill: green;");
        } else {
            productNameLabel.setText("Product not found");
            productNameLabel.setStyle("-fx-text-fill: red;");
        }
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

            addOrUpdateCartItem(productId, product, quantity);
            updateTotals();
            clearInputFields();

        } catch (NumberFormatException | ProductNotFoundException | DatabaseException e) {
            showAlert("Invalid Input", "Please enter valid numbers for ID and quantity");
        }
    }

    private void addOrUpdateCartItem(int productId, Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                cartTable.refresh();
                return;
            }
        }

        cartItems.add(new CartItem(
                productId,
                product.getName(),
                product.getSellingPrice(),
                quantity
        ));
    }

    private void removeFromCart(CartItem item) {
        cartItems.remove(item);
        updateTotals();
    }

    private void updateTotals() {
        totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        grandTotal = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();

        totalItemsLabel.setText(String.valueOf(totalItems));
        grandTotalLabel.setText(String.format("$%.2f", grandTotal));
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
            processSale();
            generateAndShowReceipt();
            handleClearCart();
        } catch (InsufficientStockException e) {
            showAlert("Insufficient Stock", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to process sale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processSale() throws DatabaseException, InsufficientStockException, ProductNotFoundException {
        // First verify stock for all items
        for (CartItem item : cartItems) {
            Product product = ProductDAO.getProductById(item.getProductId());
            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName() +
                        ". Available: " + product.getQuantity() + ", Requested: " + item.getQuantity());
            }
        }

        // Then update inventory
        for (CartItem item : cartItems) {
            ProductDAO.updateProductQuantity(
                    item.getProductId(),
                    -item.getQuantity()
            );
        }
    }

    private void generateAndShowReceipt() throws IOException {
        // Create the sale first (moved from processSale)
        Sale sale;
        try {
            sale = SaleDAO.createSale(
                    currentCashRegisterId,
                    currentEmployeeId,
                    grandTotal,
                    LocalDateTime.now()
            );
        } catch (SQLException e) {
            showAlert("Error", "Failed to create sale record: " + e.getMessage());
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/supermarket/supermarketsystemjavafx/views/Receipt.fxml"));
        Parent root = loader.load();

        ReceiptController controller = loader.getController();
        controller.setSaleData(sale, cartItems); // Now passing both required parameters

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

    public void setCashRegisterId(int cashRegisterId) {
        this.currentCashRegisterId = cashRegisterId;
    }

    public void setEmployeeId(int employeeId) {
        this.currentEmployeeId = employeeId;
    }
}