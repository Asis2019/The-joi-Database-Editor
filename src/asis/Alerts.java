package asis;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static asis.custom_objects.AsisUtils.errorDialogWindow;

public class Alerts {

    private String sceneTitle;
    private int unsavedChangesDialogButtonChoice;
    private boolean yesNoConfirmationChoice;

    void messageDialog(Class calledFrom, String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(calledFrom.getResource("fxml/dialog_message.fxml"));
            Parent root = fxmlLoader.load();

            DialogMessageController controller = fxmlLoader.getController();
            controller.inflate(message);
            Scene main_scene = new Scene(root);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            errorDialogWindow(e);
        }
    }

    boolean confirmationDialog(Class calledFrom, String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(calledFrom.getResource("fxml/dialog_confirmation.fxml"));
            Parent root = fxmlLoader.load();

            DialogConfirmation controller = fxmlLoader.getController();
            controller.inflate(this, message);
            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> yesNoConfirmationChoice = false);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            errorDialogWindow(e);
        }

        return yesNoConfirmationChoice;
    }

    void setYesNoConfirmationChoice(boolean result) {
        this.yesNoConfirmationChoice = result;
    }

    int unsavedChangesDialog(Class calledFrom, String title, String message) {
        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(calledFrom.getResource("fxml/dialog_unsaved_changes_dialog.fxml"));
            Parent root = fxmlLoader.load();

            DialogUnsavedChangesDialog controller = fxmlLoader.getController();
            controller.inflate(this, message);
            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> unsavedChangesDialogButtonChoice = 0);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            errorDialogWindow(e);
        }

        return unsavedChangesDialogButtonChoice;
    }

    void setUnsavedChangesResult(int result) {
        this.unsavedChangesDialogButtonChoice = result;
    }

    String addNewSceneDialog(Class calledFrom, String defaultTitle) {
        sceneTitle = defaultTitle;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(calledFrom.getResource("fxml/dialog_set_scene_title.fxml"));
            Parent root = fxmlLoader.load();

            DialogSceneTitleController controller = fxmlLoader.getController();
            controller.inflate(this, sceneTitle);
            Scene main_scene = new Scene(root);

            stage.setOnCloseRequest(windowEvent -> sceneTitle = null);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
            stage.setScene(main_scene);
            stage.setTitle("Scene Title");
            stage.showAndWait();
        } catch (IOException e) {
            errorDialogWindow(e);
        }

        return sceneTitle;
    }

    void setTitle(String title) {
        this.sceneTitle = title;
    }
}
