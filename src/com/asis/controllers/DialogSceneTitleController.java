package com.asis.controllers;

import com.asis.utilities.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class DialogSceneTitleController {

    private Alerts alerts;

    @FXML private TextField sceneTitleTextField;

    public void inflate(Alerts alerts, String defaultTitle) {
        this.alerts = alerts;

        sceneTitleTextField.setText(defaultTitle);
    }

    public void actionSave() {
        alerts.setTitle(sceneTitleTextField.getText().trim());
        Stage stage = (Stage) sceneTitleTextField.getScene().getWindow();
        stage.close();
    }
}
