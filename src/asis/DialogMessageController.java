package asis;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DialogMessageController {

    @FXML private Label messageLabel;

    void inflate(String title) {
        this.messageLabel.setText(title);
    }

}
