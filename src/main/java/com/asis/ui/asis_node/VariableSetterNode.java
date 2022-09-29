package com.asis.ui.asis_node;

import com.asis.controllers.EditorWindow;
import com.asis.controllers.dialogs.DialogVariableSetter;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.VariableSetter;

public class VariableSetterNode extends JOIComponentNode {

    public VariableSetterNode(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(width, height, componentId, component, editorWindow);

        setUserData("variableSetter");
        setId("Variable");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");

        setupContextMenu();
    }

    @Override
    protected boolean openDialog() {
        return DialogVariableSetter.openVariableSetter((VariableSetter) getJoiComponent());
    }
}
