package com.asis.ui.asis_node;

import com.asis.controllers.dialogs.DialogArithmetic;
import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.JOIComponent;

public class ArithmeticNode extends JOIComponentNode {

    public ArithmeticNode(int width, int height, int componentId, JOIComponent component) {
        super(width, height, componentId, component);

        setUserData("arithmetic");
        setId("Arithmetic");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");

        setupContextMenu();
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a, #5a5a5a, #5a5a5a, #273036, #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a, #5a5a5a, #5a5a5a, #273036, #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    @Override
    protected boolean openDialog() {
        return DialogArithmetic.openArithmetic((Arithmetic) getJoiComponent());
    }
}
