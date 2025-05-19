package org.supermarket.supermarketsystemjavafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class SetupController {
    @FXML
    private Button backButton;  // Matches fx:id in FXML

    @FXML
    private void initialize() {
        // Initialization code
    }

    @FXML
    private void handleBackButton() throws IOException {
        // Return to dashboard
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/SetupWindow.fxml")
        );
        Parent root = loader.load();

        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.setScene(new Scene(root));
    }
}