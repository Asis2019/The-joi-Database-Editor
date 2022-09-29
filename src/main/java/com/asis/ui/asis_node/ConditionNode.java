package com.asis.ui.asis_node;

import com.asis.controllers.EditorWindow;
import com.asis.controllers.dialogs.DialogCondition;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.JOIComponent;

public class ConditionNode extends JOIComponentNode {

    public ConditionNode(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(width, height, componentId, component, editorWindow);

        setUserData("condition");
        setId("Condition");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("True", "true_output");
        createNewOutputConnectionPoint("False", "false_output");

        setupContextMenu();
    }

    @Override
    protected boolean openDialog() {
        return DialogCondition.openConditionDialog((Condition) getJoiComponent());
    }
}
