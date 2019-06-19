package asis;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogUnsavedChangesDialog {

    @FXML private Label messageLabel;

    private Alerts alerts;

    void inflate(Alerts alerts, String title) {
        this.messageLabel.setText(title);
        this.alerts = alerts;
    }

    public void actionCancel() {
        alerts.setUnsavedChangesResult(0);
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionDoNotSave() {
        alerts.setUnsavedChangesResult(1);
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public void actionSave() {
        alerts.setUnsavedChangesResult(2);
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

}
