package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.supermarket.supermarketsystemjavafx.dao.EmployeeDAO;
import org.supermarket.supermarketsystemjavafx.dao.SaleDAO;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.exceptions.EmployeeNotFoundException;
import org.supermarket.supermarketsystemjavafx.models.Employee;
import org.supermarket.supermarketsystemjavafx.models.Sale;
import org.supermarket.supermarketsystemjavafx.models.SaleItem;
import org.supermarket.supermarketsystemjavafx.services.PricingService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptController {

    @FXML private Label saleIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label cashRegisterLabel;
    @FXML private Label employeeLabel;
    @FXML private VBox itemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label grandTotalLabel;

    public static void passSaleData(int saleId)
    {
        ReceiptController receiptController = new ReceiptController();
        receiptController.setSaleData(saleId);
    }


    public void setSaleData(int saleId) {
        try {
            // 1. Fetch sale data
            Sale sale = SaleDAO.getSaleById(saleId);
            if (sale == null) {
                showError("Sale not found");
                return;
            }

            // 2. Set basic info
            saleIdLabel.setText(String.valueOf(sale.getId()));
            dateLabel.setText(sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            cashRegisterLabel.setText(String.valueOf(sale.getCashRegisterId()));

            // 3. Get employee info
            Employee employee = EmployeeDAO.getEmployeeById(sale.getEmployeeId());
            if (employee != null) {
                employeeLabel.setText(employee.getName() + " (ID: " + employee.getId() + ")");
            } else {
                employeeLabel.setText("Unknown (ID: " + sale.getEmployeeId() + ")");
            }

            // 4. Load and display items
            List<SaleItem> items = SaleDAO.getSaleItems(saleId);
            if (items == null || items.isEmpty()) {
                showError("No items found for this sale");
                return;
            }

            displayItems(items);
            calculateAndDisplayTotals(items);

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        } catch (EmployeeNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayItems(List<SaleItem> items) {
        itemsContainer.getChildren().clear();

        for (SaleItem item : items) {
            HBox itemRow = new HBox(10);
            itemRow.setPrefWidth(360);

            // Create and add labels for each item property
            itemRow.getChildren().addAll(
                    createItemLabel(item.getProductName(), 180),
                    createItemLabel(String.valueOf(item.getQuantity()), 50, true),
                    createItemLabel(String.format("$%.2f", item.getUnitPrice()), 60, true),
                    createItemLabel(String.format("$%.2f", item.getUnitPrice() * item.getQuantity()), 70, true)
            );

            itemsContainer.getChildren().add(itemRow);
        }
    }

    private Label createItemLabel(String text, double width) {
        return createItemLabel(text, width, false);
    }

    private Label createItemLabel(String text, double width, boolean rightAlign) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        if (rightAlign) {
            label.setStyle("-fx-alignment: CENTER_RIGHT;");
        }
        return label;
    }

    private void calculateAndDisplayTotals(List<SaleItem> items) {
        double subtotal = items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        double discountRate = PricingService.getPricingConfig().getExpirationDiscountPercent();
        double discount = subtotal * discountRate;
        double grandTotal = subtotal - discount;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f (%.0f%%)", discount, discountRate * 100));
        grandTotalLabel.setText(String.format("$%.2f", grandTotal));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
