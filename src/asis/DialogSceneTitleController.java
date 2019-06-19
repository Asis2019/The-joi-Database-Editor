package asis;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class DialogSceneTitleController {

    private Alerts alerts;

    @FXML private TextField sceneTitleTextField;

    void inflate(Alerts alerts) {
        this.alerts = alerts;
    }

    public void actionUseDefault() {
        Stage stage = (Stage) sceneTitleTextField.getScene().getWindow();
        stage.close();
    }

    public void actionSave() {
        alerts.setTitle(sceneTitleTextField.getText().trim());
        Stage stage = (Stage) sceneTitleTextField.getScene().getWindow();
        stage.close();
    }
}
