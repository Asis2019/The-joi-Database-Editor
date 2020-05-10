package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.utilities.AsisUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;


public class DialogSceneTitle {
    @FXML private TextField sceneTitleTextField;
    private static String sceneTitle;

    public void inflate(String defaultTitle) {
        sceneTitleTextField.setText(defaultTitle);
    }

    public void actionSave() {
        sceneTitle = sceneTitleTextField.getText().trim();
        Stage stage = (Stage) sceneTitleTextField.getScene().getWindow();
        stage.close();
    }

    public static String addNewSceneDialog(String defaultTitle) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_set_scene_title.fxml"));
            Parent root = fxmlLoader.load();

            DialogSceneTitle controller = fxmlLoader.getController();
            controller.inflate(defaultTitle);
            Scene main_scene = new Scene(root);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle("Scene Title");
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return sceneTitle;
    }
}
