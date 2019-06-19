package asis;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class Alerts {

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
}
