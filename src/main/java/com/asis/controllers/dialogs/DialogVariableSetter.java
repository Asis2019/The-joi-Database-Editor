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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static com.asis.controllers.dialogs.DialogCondition.setVariableValue;
import static com.asis.utilities.AsisUtils.getDefaultTitle;

public class DialogVariableSetter {

    @FXML
    private TextField variableNameField, variableValueField, variableSetterTitle;
    @FXML
    private CheckBox variablePersistentBox;

    private VariableSetter variableSetter;

    private static boolean result = true;

    public void init() {
        variableSetterTitle.setText(getDefaultTitle(variableSetter, "Variable"));
        variableNameField.setText(variableSetter.getVariableName());
        variableValueField.setText(String.valueOf(variableSetter.getVariableValue()));
        variablePersistentBox.setSelected(variableSetter.isVariablePersistent());
    }

    public void actionSave() {
        try {
            variableSetter.setComponentTitle(variableSetterTitle.getText().trim());
            variableSetter.setVariableName(variableNameField.getText().trim());
            setVariableValue(variableValueField, variableSetter);
            variableSetter.setVariablePersistent(variablePersistentBox.isSelected());

            Stage stage = (Stage) variableSetterTitle.getScene().getWindow();
            StageManager.getInstance().closeStage(stage);
        } catch (NullPointerException e) {
            DialogMessage.messageDialog("Unable to save variable", "Please ensure that the variable name is not empty.", 600, 200);
        }
    }

    public void setVariableSetter(VariableSetter variableSetter) {
        this.variableSetter = variableSetter;
    }

    public static boolean openVariableSetter(VariableSetter variableSetter) {
        result = true;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/dialog_variable_setter.fxml"));
            Parent root = fxmlLoader.load();

            DialogVariableSetter controller = fxmlLoader.getController();
            controller.setVariableSetter(variableSetter);
            controller.init();

            Scene main_scene = new Scene(root);

            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
            stage.setScene(main_scene);
            stage.setUserData(variableSetter.getComponentId());
            stage.setTitle("Variable Setter");
            stage.setOnCloseRequest(windowEvent -> result = false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return result;
    }

}
