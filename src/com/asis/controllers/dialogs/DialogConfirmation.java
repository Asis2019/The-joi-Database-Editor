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

public class DialogConfirmation {

    @FXML
    private Label messageLabel;
    private static boolean option;

    public void setMessageLabelText(String title) {
        this.messageLabel.setText(title);
    }

    public void actionYes() {
        option = true;
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionNo() {
        option = false;
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public static boolean show(String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_confirmation.fxml"));
            Parent root = fxmlLoader.load();

            DialogConfirmation controller = fxmlLoader.getController();
            controller.setMessageLabelText(message);

            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> option=false);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return option;
    }
}
