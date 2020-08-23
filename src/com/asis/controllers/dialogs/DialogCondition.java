package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.controllers.Controller;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class DialogCondition {

    @FXML
    private ComboBox<Condition.ConditionType> operationDropdown;
    @FXML
    private ComboBox<String> variable1Dropdown, variable2Dropdown;
    @FXML
    private TextField conditionTitle;
    private Condition condition;

    public void init() {
        conditionTitle.setText(condition.getComponentTitle());

        operationDropdown.getItems().addAll(Condition.ConditionType.values());
        operationDropdown.getSelectionModel().select(condition.getConditionType());

        for(JOIComponent component: Controller.getInstance().getJoiPackage().getJoi().getJoiComponents()) {
            if(component instanceof VariableSetter) {
                variable1Dropdown.getItems().add(((VariableSetter) component).getVariableName());
                variable2Dropdown.getItems().add(((VariableSetter) component).getVariableName());
            }
        }

        if(condition.getFirstVariable() != null) variable1Dropdown.getSelectionModel().select(condition.getFirstVariable());
        if(condition.getSecondVariable() != null) variable2Dropdown.getSelectionModel().select(condition.getSecondVariable());
    }

    public void actionSave() {
        try {
            condition.setComponentTitle(conditionTitle.getText().trim());
            condition.setConditionType(operationDropdown.getValue());
            condition.setFirstVariable(variable1Dropdown.getValue());
            condition.setSecondVariable(variable2Dropdown.getValue());

            Stage stage = (Stage) conditionTitle.getScene().getWindow();
            StageManager.getInstance().closeStage(stage);
        } catch (NullPointerException e) {
            //DialogMessage.messageDialog("Error", "Please ensure that the title, name and value are not empty.");
        }
    }

    public static void openConditionDialog(Condition condition) {
        if (StageManager.getInstance().requestStageFocus(condition.getComponentId())) return;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_condition.fxml"));
            Parent root = fxmlLoader.load();

            DialogCondition controller = fxmlLoader.getController();
            controller.setCondition(condition);
            controller.init();

            Scene main_scene = new Scene(root);

            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(main_scene);
            stage.setUserData(condition.getComponentId());
            stage.setTitle("Condition");

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
