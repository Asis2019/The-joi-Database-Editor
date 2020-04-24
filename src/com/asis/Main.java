package com.asis;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogNewProject;
import com.asis.utilities.Alerts;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/icon.png")));
        primaryStage.setScene(new Scene(new FXMLLoader(getClass().getResource("/resources/fxml/Main.fxml")).load(), 1280, 720));
        primaryStage.setTitle("The joi Database Editor");
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(480);
        primaryStage.show();
        primaryStage.setMaximized(true);

        DialogNewProject.newProjectWindow(true);

        primaryStage.setOnCloseRequest(event -> {
            if (Controller.getInstance().changesHaveOccurred()) {
                int choice = new Alerts().unsavedChangesDialog("Warning", "You have unsaved work, are you sure you want to quit?");
                switch (choice) {
                    case 0:
                        event.consume();
                        return;

                    case 2:
                        Controller.getInstance().actionSaveProject();
                        break;
                }
            }

            Controller.getInstance().quiteProgram();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
