package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.supermarket.supermarketsystemjavafx.dao.ProductDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Product;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // FXML Components
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;

    @FXML private Button salesButton;
    @FXML private Button setupButton;
    @FXML private Button deleteSelectedButton;
    @FXML private CheckBox selectAllCheckBox;

    @FXML private Tab employeesTab;
    @FXML private Tab inventoryTab;

    // Data
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<Product> selectedProducts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        setupTableColumns();

        // Setup selection functionality
        setupSelectionColumn();

        // Setup delete button
        setupDeleteButton();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup context menu
        setupContextMenu();

        // Load initial data
        loadProductsFromDatabase();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().sellingPriceProperty().asObject());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty().asString());
        barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().barcodeProperty());
    }

    private void setupSelectionColumn() {
        // Checkbox column for selection
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(column -> new TableCell<Product, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    product.setSelected(checkBox.isSelected());
                    updateSelectedProductsList();
                });
            }

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(selected != null && selected);
                    setGraphic(checkBox);
                }
            }
        });

        // Highlight selected rows
        productsTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isSelected()) {
                    setStyle("-fx-background-color: #ffeeee;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupDeleteButton() {
        deleteSelectedButton.setDisable(true);
        deleteSelectedButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
    }

    private void setupKeyboardShortcuts() {
        productsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDeleteSelected();
            }
        });
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> handleDeleteSelected());
        contextMenu.getItems().add(deleteItem);
        productsTable.setContextMenu(contextMenu);
    }

    private void loadProductsFromDatabase() {
        try {
            products.setAll(ProductDAO.getAllProducts());
            productsTable.setItems(products);
        } catch (DatabaseException e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSelectedProductsList() {
        selectedProducts.setAll(
                products.filtered(Product::isSelected)
        );
        deleteSelectedButton.setDisable(selectedProducts.isEmpty());
        selectAllCheckBox.setSelected(selectedProducts.size() == products.size() && !products.isEmpty());
    }

    @FXML
    private void handleSelectAll() {
        boolean selectAll = selectAllCheckBox.isSelected();
        products.forEach(p -> p.setSelected(selectAll));
        productsTable.refresh();
        updateSelectedProductsList();
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedProducts.isEmpty()) return;

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Selected Products");
        confirmation.setContentText(String.format("Are you sure you want to delete %d selected products?",
                selectedProducts.size()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                for (Product product : selectedProducts) {
                    ProductDAO.deleteProduct(product.getId());
                }

                // Remove from observable list
                products.removeAll(selectedProducts);
                selectedProducts.clear();
                selectAllCheckBox.setSelected(false);

                // Show success
                showAlert("Success", "Selected products deleted successfully");

            } catch (DatabaseException | ProductNotFoundException e) {
                showAlert("Error", "Failed to delete some products: " + e.getMessage());
            }
        }
    }

    @FXML
    private void sellButtonClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SalesWindow.fxml")
        );
        openWindow(loader, "Sales Management", salesButton);
    }

    @FXML
    private void setupButtonClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SetupWindow.fxml")
        );
        openWindow(loader, "Setup", setupButton);
    }

    @FXML
    private void addButtonClicked() throws IOException {
        FXMLLoader loader;
        if (inventoryTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddItem.fxml")
            );
        } else if (employeesTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddEmployee.fxml")
            );
        } else {
            return;
        }

        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Add");
        stage.setScene(new Scene(root));
        stage.setOnHidden(e -> refreshProducts());
        stage.showAndWait();
    }

    private void openWindow(FXMLLoader loader, String title, Button ownerButton) throws IOException {
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initOwner(ownerButton.getScene().getWindow());
        stage.show();
    }

    private void refreshProducts() {
        try {
            products.setAll(ProductDAO.getAllProducts());
            productsTable.refresh();
            selectedProducts.clear();
            selectAllCheckBox.setSelected(false);

        } catch (DatabaseException e) {
            showAlert("Refresh Error", "Could not refresh products: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}