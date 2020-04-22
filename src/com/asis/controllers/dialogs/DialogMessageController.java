package com.asis.controllers.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DialogMessageController {

    @FXML private Label messageLabel;

    public void inflate(String title) {
        this.messageLabel.setText(title);
    }

}
