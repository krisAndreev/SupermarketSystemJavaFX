package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.supermarket.supermarketsystemjavafx.exceptions.DatabaseException;
import org.supermarket.supermarketsystemjavafx.models.PricingConfig;
import org.supermarket.supermarketsystemjavafx.services.PricingService;

public class SetupController {
    @FXML private Spinner<Double> foodMarkupSpinner;
    @FXML private Spinner<Double> nonFoodMarkupSpinner;
    @FXML private Spinner<Integer> expirationThresholdSpinner;
    @FXML private Spinner<Double> expirationDiscountSpinner;

    public void initialize() {
        // Load current configuration
        PricingConfig config = PricingService.getPricingConfig();

        // Set up spinners
        foodMarkupSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, (config.getFoodMarkupPercent()*100), 1));
        nonFoodMarkupSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, (config.getNonFoodMarkupPercent()*100), 1));
        expirationThresholdSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, config.getExpirationThresholdDays()));
        expirationDiscountSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, (config.getExpirationDiscountPercent()*100), 1));
    }

    @FXML
    private void handleSave() throws DatabaseException {
        PricingConfig newConfig = new PricingConfig(
                (foodMarkupSpinner.getValue()/100),
                (nonFoodMarkupSpinner.getValue()/100),
                expirationThresholdSpinner.getValue(),
                (expirationDiscountSpinner.getValue()/100)
        );

        PricingService.updatePricingConfig(newConfig);
        // Close the window or show success message
        showAlert("Success", "Setup parameters updated");
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) foodMarkupSpinner.getScene().getWindow();
        stage.close();
    }
}