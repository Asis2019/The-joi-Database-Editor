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

public class DialogMessage {

    @FXML private Label messageLabel;

    public void setMessageLabel(String title) {
        this.messageLabel.setText(title);
    }

    public static void messageDialog(String title, String message, int... optional) {
        int width = 600;
        int height = 400;

        if(optional.length == 2) {
            width = optional[0];
            height = optional[1];
        }

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_message.fxml"));
            Parent root = fxmlLoader.load();

            DialogMessage controller = fxmlLoader.getController();
            controller.setMessageLabel(message);
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

}
