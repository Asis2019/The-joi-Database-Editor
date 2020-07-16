package com.asis.ui.asis_node;

import com.asis.joi.model.entities.JOIComponent;

public class VariableSetterNode extends JOIComponentNode {

    public VariableSetterNode(int width, int height, int componentId, SceneNodeMainController sceneNodeMainController, JOIComponent component) {
        super(width, height, componentId, sceneNodeMainController, component);

        setUserData("variableSetter");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");
    }

    protected void focusState(boolean value) {
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
