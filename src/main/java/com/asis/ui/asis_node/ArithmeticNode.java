package com.asis.ui.asis_node;

import com.asis.controllers.EditorWindow;
import com.asis.controllers.dialogs.DialogArithmetic;
import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.JOIComponent;

public class ArithmeticNode extends JOIComponentNode {

    public ArithmeticNode(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(width, height, componentId, component, editorWindow);

        setUserData("arithmetic");
        setId("Arithmetic");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");

        setupContextMenu();
    }

    @Override
    protected boolean openDialog() {
        return DialogArithmetic.openArithmetic((Arithmetic) getJoiComponent());
    }
}
