package org.supermarket.supermarketsystemjavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.supermarket.supermarketsystemjavafx.db.DatabaseInitializer;

public class MainApp extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        DatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/supermarket/supermarketsystemjavafx/Dashboard.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("Supermarket System");
        primaryStage.setScene(new Scene(root, 1089, 648));
        primaryStage.resizableProperty().setValue(Boolean.FALSE);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}