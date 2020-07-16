package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class DialogVariableSetter {

    @FXML
    private TextField variableNameField, variableValueField, variableSetterTitle;
    @FXML
    private CheckBox variablePersistentBox;

    private VariableSetter variableSetter;

    public void init() {
        variableSetterTitle.setText(variableSetter.getComponentTitle());
        variableNameField.setText(variableSetter.getVariableName());
        variableValueField.setText(String.valueOf(variableSetter.getVariableValue()));
        variablePersistentBox.setSelected(variableSetter.isVariablePersistent());
    }

    public void actionSave() {
        try {
            variableSetter.setComponentTitle(variableSetterTitle.getText().trim());
            variableSetter.setVariableName(variableNameField.getText().trim());
            setVariableValue();
            variableSetter.setVariablePersistent(variablePersistentBox.isSelected());

            Stage stage = (Stage) variableSetterTitle.getScene().getWindow();
            stage.close();
        } catch (NullPointerException e) {
            DialogMessage.messageDialog("Error", "Please ensure that the title, name and value are not empty.");
        }
    }

    private void setVariableValue() {
        //Converts string into int or bool
        final String conversionValue = variableValueField.getText().trim();
        Object finalValue;

        if (conversionValue.equalsIgnoreCase("true") || conversionValue.equalsIgnoreCase("false")) {
            finalValue = Boolean.valueOf(conversionValue);
        } else {
            try {
                finalValue = Integer.parseInt(conversionValue);
            } catch (NumberFormatException e) {
                finalValue = conversionValue;
            }
        }

        variableSetter.setVariableValue(finalValue);
    }

    public void setVariableSetter(VariableSetter variableSetter) {
        this.variableSetter = variableSetter;
    }

    public static void openVariableSetter(VariableSetter variableSetter) {
        if (StageManager.getInstance().requestStageFocus(variableSetter.getComponentId())) return;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_variable_setter.fxml"));
            Parent root = fxmlLoader.load();

            DialogVariableSetter controller = fxmlLoader.getController();
            controller.setVariableSetter(variableSetter);
            controller.init();

            Scene main_scene = new Scene(root);

            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setUserData(variableSetter.getComponentId());
            stage.setTitle("Variable Setter");

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

}
