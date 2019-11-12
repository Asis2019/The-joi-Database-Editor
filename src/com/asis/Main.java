package com.asis;

import com.asis.controllers.Controller;
import com.asis.utilities.Alerts;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = new FXMLLoader(getClass().getResource("/resources/fxml/Main.fxml")).load();

        Scene main_scene = new Scene(root, 1280, 720);

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/icon.png")));
        primaryStage.setScene(main_scene);
        primaryStage.setTitle("The joi Database Editor");
        primaryStage.show();
        primaryStage.setMaximized(true);

        Alerts.newProjectWindow(true);

        primaryStage.setOnCloseRequest(event -> {
            if (Controller.getInstance().changesHaveOccurred()) {
                int choice = new Alerts().unsavedChangesDialog("Warning", "You have unsaved work, are you sure you want to quit?");
                switch (choice) {
                    case 0:
                        event.consume();
                        break;

                    case 1:
                        Controller.getInstance().quiteProgram();
                        break;

                    case 2:
                        Controller.getInstance().actionSaveProject();
                        Controller.getInstance().quiteProgram();
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
