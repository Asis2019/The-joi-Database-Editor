package com.asis.ui.asis_node;

import com.asis.controllers.dialogs.DialogVariableSetter;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.VariableSetter;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class VariableSetterNode extends JOIComponentNode {

    public VariableSetterNode(int width, int height, int componentId, JOIComponent component) {
        super(width, height, componentId, component);

        setUserData("variableSetter");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");

        setupContextMenu();
    }

    @Override
    protected void setupContextMenu() {
        MenuItem editSceneItem = new MenuItem("Edit Variable");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem deleteNodeItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editSceneItem, separatorMenuItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                DialogVariableSetter.openVariableSetter((VariableSetter) getJoiComponent());
            }
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                ComponentNodeManager.getInstance().removeComponentNode(this);
            }
        });
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a, rgb(60, 63, 65), #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a, rgb(60, 63, 65), #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }
}
