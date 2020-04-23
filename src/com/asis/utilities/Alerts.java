package com.asis.utilities;

import com.asis.Main;
import com.asis.controllers.dialogs.DialogConfirmation;
import com.asis.controllers.dialogs.DialogMessageController;
import com.asis.controllers.dialogs.DialogUnsavedChanges;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Alerts {
    //TODO these methods should be moved to there respective controllers
    private int unsavedChangesDialogButtonChoice;
    private boolean yesNoConfirmationChoice;

    public static void messageDialog(String title, String message) {
        messageDialog(title, message, 600, 400);
    }

    public static void messageDialog(String title, String message, int width, int height) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Alerts.class.getResource("/resources/fxml/dialog_message.fxml"));
            Parent root = fxmlLoader.load();

            DialogMessageController controller = fxmlLoader.getController();
            controller.inflate(message);
            Scene main_scene = new Scene(root, width, height);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public boolean confirmationDialog(String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Alerts.class.getResource("/resources/fxml/dialog_confirmation.fxml"));
            Parent root = fxmlLoader.load();

            DialogConfirmation controller = fxmlLoader.getController();
            controller.inflate(this, message);
            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> yesNoConfirmationChoice = false);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return yesNoConfirmationChoice;
    }

    public void setYesNoConfirmationChoice(boolean result) {
        this.yesNoConfirmationChoice = result;
    }

    public int unsavedChangesDialog(String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Alerts.class.getResource("/resources/fxml/dialog_unsaved_changes_dialog.fxml"));
            Parent root = fxmlLoader.load();

            DialogUnsavedChanges controller = fxmlLoader.getController();
            controller.inflate(this, message);
            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> unsavedChangesDialogButtonChoice = 0);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return unsavedChangesDialogButtonChoice;
    }

    public void setUnsavedChangesResult(int result) {
        this.unsavedChangesDialogButtonChoice = result;
    }
}
