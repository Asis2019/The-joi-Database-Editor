package asis;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.util.Optional;

public class Alerts {

    private Alerts instance;

    public Alerts() {
        instance = this;
    }

    public static void warningDialog(String title, String header, String content, Stage primaryStage, WindowEvent... event) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);


        ButtonType btnType1 = new ButtonType("Save Project");
        ButtonType btnType2 = new ButtonType("Continue Without Saving");
        ButtonType btnCancel= new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnType1, btnType2, btnCancel);

        Alerts alerts = new Alerts();

        alerts.setIcon(alert);


        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == btnType1){
            Controller.getInstance().actionSaveProject();
        } else if (result.get() == btnType2){
            alert.close();
        } else {
            event[0].consume();
        }
    }

    public void setIcon(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("images/icon.png").toString()));
    }
}
