package asis;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static asis.custom_objects.AsisUtils.errorDialogWindow;

public class Alerts {

    private String sceneTitle;

    public static void confirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType btnType1 = new ButtonType("Save Project");
        ButtonType btnType2 = new ButtonType("Continue Without Saving");
        ButtonType btnCancel= new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnType1, btnType2, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == btnType1){
            Controller.getInstance().actionSaveProject();
        } else {
            alert.close();
        }
    }

    public String addNewSceneDialog(Class calledFrom, String defaultTitle) {
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
