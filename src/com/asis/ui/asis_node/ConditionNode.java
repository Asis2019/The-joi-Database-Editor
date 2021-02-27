package com.asis.ui.asis_node;

import com.asis.controllers.dialogs.DialogCondition;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.JOIComponent;

public class ConditionNode extends JOIComponentNode {

    public ConditionNode(int width, int height, int componentId, JOIComponent component) {
        super(width, height, componentId, component);

        setUserData("condition");
        setId("Condition");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("True", "true_output");
        createNewOutputConnectionPoint("False", "false_output");

        setupContextMenu();
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

    @Override
    protected boolean openDialog() {
        return DialogCondition.openConditionDialog((Condition) getJoiComponent());
    }
}
