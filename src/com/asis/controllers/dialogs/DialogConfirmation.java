package com.asis.controllers.dialogs;

import com.asis.utilities.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogConfirmation {

    @FXML
    private Label messageLabel;

    private Alerts alerts;

    public void inflate(Alerts alerts, String title) {
        this.messageLabel.setText(title);
        this.alerts = alerts;
    }

    public void actionYes() {
        alerts.setYesNoConfirmationChoice(true);
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionNo() {
        alerts.setYesNoConfirmationChoice(false);
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
