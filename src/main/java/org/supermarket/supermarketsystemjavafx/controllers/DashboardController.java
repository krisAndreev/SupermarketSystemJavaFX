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
import org.supermarket.supermarketsystemjavafx.dao.EmployeeDAO;
import org.supermarket.supermarketsystemjavafx.dao.ProductDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.EmployeeNotFoundException;
import org.supermarket.supermarketsystemjavafx.exceptions.ProductNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Employee;
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

    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, Integer> idColumnE;
    @FXML private TableColumn<Employee, String> nameColumnE;
    @FXML private TableColumn<Employee, Double> salaryColumnE;
    @FXML private TableColumn<Employee, String> pinColumnE;
    @FXML private TableColumn<Employee, Boolean> selectColumnE;

    @FXML private Button salesButton;
    @FXML private Button setupButton;
    @FXML private Button deleteSelectedButton;
    @FXML private Button addButton;
    @FXML private CheckBox selectAllCheckBox;

    @FXML private Tab employeesTab;
    @FXML private Tab inventoryTab;

    // Data
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<Product> selectedProducts = FXCollections.observableArrayList();

    private ObservableList<Employee> employees = FXCollections.observableArrayList();
    private ObservableList<Employee> selectedEmployees = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        setupTableColumns();

        // Setup selection functionality
        setupSelectionColumns();

        // Setup delete button
        setupDeleteButton();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup context menu
        setupContextMenu();

        // Load initial data for the default tab (Inventory)
        loadProductsFromDatabase();

        // Listen for tab changes
        setupTabListener();
    }

    private void setupTableColumns() {
        // Products table
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().sellingPriceProperty().asObject());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty().asString());
        barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().barcodeProperty());

        // Employees table
        idColumnE.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumnE.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        salaryColumnE.setCellValueFactory(cellData -> cellData.getValue().monthlySalaryProperty().asObject());
        //pinColumnE.setCellValueFactory(cellData -> cellData.getValue().pinProperty());
    }

    private void setupSelectionColumns() {
        // Products selection column
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(column -> createSelectionCell(selectedProducts));

        // Employees selection column
        selectColumnE.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumnE.setCellFactory(column -> createSelectionCell(selectedEmployees));

        // Highlight selected rows for both tables
        setupRowHighlighting(productsTable, selectedProducts);
        setupRowHighlighting(employeesTable, selectedEmployees);
    }

    private <T> TableCell<T, Boolean> createSelectionCell(ObservableList<T> selectedItems) {
        return new TableCell<T, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    T item = getTableView().getItems().get(getIndex());
                    if (item instanceof Product) ((Product) item).setSelected(checkBox.isSelected());
                    else if (item instanceof Employee) ((Employee) item).setSelected(checkBox.isSelected());
                    updateSelectedItemsList(selectedItems);
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
        };
    }

    private <T> void setupRowHighlighting(TableView<T> tableView, ObservableList<T> selectedItems) {
        tableView.setRowFactory(tv -> new TableRow<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    boolean isSelected = false;
                    if (item instanceof Product) isSelected = ((Product) item).isSelected();
                    else if (item instanceof Employee) isSelected = ((Employee) item).isSelected();

                    if (isSelected) {
                        setStyle("-fx-background-color: #ffeeee;");
                    } else {
                        setStyle("");
                    }
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

        employeesTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDeleteSelected();
            }
        });
    }

    private void setupContextMenu() {
        // Products context menu
        ContextMenu productsContextMenu = new ContextMenu();
        MenuItem deleteProductItem = new MenuItem("Delete");
        deleteProductItem.setOnAction(e -> handleDeleteSelected());
        productsContextMenu.getItems().add(deleteProductItem);
        productsTable.setContextMenu(productsContextMenu);

        // Employees context menu
        ContextMenu employeesContextMenu = new ContextMenu();
        MenuItem deleteEmployeeItem = new MenuItem("Delete");
        deleteEmployeeItem.setOnAction(e -> handleDeleteSelected());
        employeesContextMenu.getItems().add(deleteEmployeeItem);
        employeesTable.setContextMenu(employeesContextMenu);
    }

    private void setupTabListener() {
        inventoryTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) loadProductsFromDatabase();
        });

        employeesTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) loadEmployeesFromDatabase();
        });
    }

    private void loadProductsFromDatabase() {
        try {
            products.setAll(ProductDAO.getAllProducts());
            productsTable.setItems(products);
            selectedProducts.clear();
            updateDeleteButtonState();
        } catch (DatabaseException e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEmployeesFromDatabase() {
        try {
            employees.setAll(EmployeeDAO.getAllEmployees());
            employeesTable.setItems(employees);
            selectedEmployees.clear();
            updateDeleteButtonState();
        } catch (DatabaseException e) {
            showAlert("Database Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> void updateSelectedItemsList(ObservableList<T> selectedItems) {
        if (inventoryTab.isSelected()) {
            selectedProducts.setAll(products.filtered(Product::isSelected));
            selectAllCheckBox.setSelected(selectedProducts.size() == products.size() && !products.isEmpty());
        } else if (employeesTab.isSelected()) {
            selectedEmployees.setAll(employees.filtered(Employee::isSelected));
            selectAllCheckBox.setSelected(selectedEmployees.size() == employees.size() && !employees.isEmpty());
        }
        updateDeleteButtonState();
    }

    private void updateDeleteButtonState() {
        if (inventoryTab.isSelected()) {
            deleteSelectedButton.setDisable(selectedProducts.isEmpty());
        } else if (employeesTab.isSelected()) {
            deleteSelectedButton.setDisable(selectedEmployees.isEmpty());
        }
    }

    @FXML
    private void handleSelectAll() {
        boolean selectAll = selectAllCheckBox.isSelected();
        if (inventoryTab.isSelected()) {
            products.forEach(p -> p.setSelected(selectAll));
            productsTable.refresh();
            updateSelectedItemsList(selectedProducts);
        } else if (employeesTab.isSelected()) {
            employees.forEach(e -> e.setSelected(selectAll));
            employeesTable.refresh();
            updateSelectedItemsList(selectedEmployees);
        }
    }

    @FXML
    private void handleDeleteSelected() {
        if (inventoryTab.isSelected() && selectedProducts.isEmpty()) return;
        if (employeesTab.isSelected() && selectedEmployees.isEmpty()) return;

        String itemType = inventoryTab.isSelected() ? "products" : "employees";
        int count = inventoryTab.isSelected() ? selectedProducts.size() : selectedEmployees.size();

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Selected " + itemType);
        confirmation.setContentText(String.format("Are you sure you want to delete %d selected %s?", count, itemType));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (inventoryTab.isSelected()) {
                    // Delete products from database
                    for (Product product : selectedProducts) {
                        ProductDAO.deleteProduct(product.getId());
                    }
                    // Remove from observable list
                    products.removeAll(selectedProducts);
                    selectedProducts.clear();
                } else if (employeesTab.isSelected()) {
                    // Delete employees from database
                    for (Employee employee : selectedEmployees) {
                        EmployeeDAO.deleteEmployee(employee.getId());
                    }
                    // Remove from observable list
                    employees.removeAll(selectedEmployees);
                    selectedEmployees.clear();
                }

                selectAllCheckBox.setSelected(false);
                showAlert("Success", "Selected " + itemType + " deleted successfully");

            } catch (DatabaseException | ProductNotFoundException e) {
                showAlert("Error", "Failed to delete some items: " + e.getMessage());
            } catch (EmployeeNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void sellButtonClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SalesWindow.fxml")
        );
        openWindow(loader, "Sales Management", salesButton, 0);
    }

    @FXML
    private void setupButtonClicked() throws IOException {
        FXMLLoader loader;
        int functionKey;

        if (inventoryTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/SetupWindow.fxml")
            );
            functionKey = 1;
        } else if (employeesTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/SetupWindow.fxml")
            );
            functionKey = 2;
        } else {
            return;
        }

        openWindow(loader, "Setup", setupButton, functionKey);
    }

    @FXML
    private void addButtonClicked() throws IOException {
        FXMLLoader loader;
        String windowTitle;
        int functionKey;
        if (inventoryTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddItem.fxml")
            );
            windowTitle = "Add item";
            functionKey = 1;
        } else if (employeesTab.isSelected()) {
            loader = new FXMLLoader(
                    getClass().getResource("/org/supermarket/supermarketsystemjavafx/AddEmployee.fxml")
            );
            windowTitle = "Add employee";
            functionKey = 2;
        } else {
            return;
        }

        openWindow(loader, windowTitle, addButton, functionKey);
    }

    private void openWindow(FXMLLoader loader, String title, Button ownerButton, int functionKey) throws IOException {
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initOwner(ownerButton.getScene().getWindow());

        switch (functionKey) {
            case 1: stage.setOnHidden(e -> refreshProducts()); break;
            case 2: stage.setOnHidden(e -> refreshEmployees()); break;
            default: break;
        }
        stage.showAndWait();
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

    private void refreshEmployees() {
        try {
            employees.setAll(EmployeeDAO.getAllEmployees());
            employeesTable.refresh();
            selectedEmployees.clear();
            selectAllCheckBox.setSelected(false);
        } catch (DatabaseException e) {
            showAlert("Refresh Error", "Could not refresh employees: " + e.getMessage());
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