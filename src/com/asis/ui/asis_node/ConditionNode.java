package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogCondition;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.JOIComponent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class ConditionNode extends JOIComponentNode {

    public ConditionNode(int width, int height, int componentId, SceneNodeMainController sceneNodeMainController, JOIComponent component) {
        super(width, height, componentId, sceneNodeMainController, component);

        setUserData("condition");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("True", "true_output");
        createNewOutputConnectionPoint("False", "false_output");

        setupContextMenu();
    }

    @Override
    protected void setupContextMenu() {
        Controller controller = Controller.getInstance();
        MenuItem editSceneItem = new MenuItem("Edit Condition");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem deleteNodeItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editSceneItem, separatorMenuItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) DialogCondition.openConditionDialog((Condition) getJoiComponent());
        });
        deleteNodeItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                controller.removeComponentNode(this);
            }
        });
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a, rgb(60, 63, 65), #5a5a5a, rgb(60, 63, 65), #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a, rgb(60, 63, 65), #5a5a5a, rgb(60, 63, 65), #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }
}
