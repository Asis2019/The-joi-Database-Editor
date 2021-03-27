package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.joi.LoadJOIService;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.utilities.AsisUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.asis.utilities.AsisUtils.getDefaultTitle;

public class DialogCondition {

    @FXML
    private ComboBox<Condition.ConditionType> operationDropdown;
    @FXML
    private ComboBox<String> variableDropdown;
    @FXML
    private TextField conditionTitle, compareValue;

    private Condition condition;
    private static boolean result = true;

    public void init() {
        conditionTitle.setText(getDefaultTitle(condition, "Condition"));

        operationDropdown.getItems().addAll(Condition.ConditionType.values());
        operationDropdown.getSelectionModel().select(condition.getConditionType());

        Set<String> listItems = new HashSet<>();
        for (JOIComponent component : LoadJOIService.getInstance().getJoiPackage().getJoi().getJoiComponents()) {
            if (component instanceof VariableSetter) {
                listItems.add(((VariableSetter) component).getVariableName());
            }
        }
        variableDropdown.getItems().addAll(listItems);

        if (condition.getVariable() != null) variableDropdown.getSelectionModel().select(condition.getVariable());
        compareValue.setText(String.valueOf(condition.getComparingValue()));
    }

    public void actionSave() {
        try {
            condition.setComponentTitle(conditionTitle.getText().trim());
            condition.setConditionType(operationDropdown.getValue());
            setVariableValue(compareValue, condition);
            condition.setVariable(variableDropdown.getValue());

            Stage stage = (Stage) conditionTitle.getScene().getWindow();
            stage.close();
        } catch (NullPointerException e) {
            DialogMessage.messageDialog("Error", "Please make sure the Node Title is filled.", 600, 200);
        }
    }

    public static void setVariableValue(TextField textField, JOIComponent joiComponent) {
        //Converts string into int or bool
        final String conversionValue = textField.getText().trim();
        Object finalValue;

        if (conversionValue.equalsIgnoreCase("true") || conversionValue.equalsIgnoreCase("false")) {
            finalValue = Boolean.valueOf(conversionValue);
        } else {
            try {
                finalValue = Double.parseDouble(conversionValue);
            } catch (NumberFormatException ignore) {
                finalValue = conversionValue;
            }
        }

        if (joiComponent instanceof Condition)
            ((Condition) joiComponent).setComparingValue(finalValue);
        else if (joiComponent instanceof VariableSetter)
            ((VariableSetter) joiComponent).setVariableValue(finalValue);
    }

    public static boolean openConditionDialog(Condition condition) {
        result = true;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_condition.fxml"));
            Parent root = fxmlLoader.load();

            DialogCondition controller = fxmlLoader.getController();
            controller.condition = condition;
            controller.init();

            Scene main_scene = new Scene(root);

            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setUserData(condition.getComponentId());
            stage.setTitle("Condition");
            stage.setOnCloseRequest(windowEvent -> result = false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return result;
    }
}
