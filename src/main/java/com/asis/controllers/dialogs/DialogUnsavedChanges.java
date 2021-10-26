package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.utilities.AsisUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DialogUnsavedChanges {

    @FXML
    private Label messageLabel;
    private static int choice;

    public static final int CHOICE_CANCEL = 0;
    public static final int CHOICE_DO_NOT_SAVE = 1;
    public static final int CHOICE_SAVE = 2;

    public void setMessageLabel(String title) {
        this.messageLabel.setText(title);
    }

    public void actionCancel() {
        choice = CHOICE_CANCEL;
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionDoNotSave() {
        choice = CHOICE_DO_NOT_SAVE;
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionSave() {
        choice = CHOICE_SAVE;
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();

    }

    public static int unsavedChangesDialog(String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/dialog_unsaved_changes_dialog.fxml"));
            Parent root = fxmlLoader.load();

            DialogUnsavedChanges controller = fxmlLoader.getController();
            controller.setMessageLabel(message);

            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> choice = CHOICE_CANCEL);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return choice;
    }

}
