package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.ComponentConnectionManager;

/**
 * This class is used to create connections with ComponentConnectionManager between components.
 */
public class CreateComponentConnectionsResolver implements ComponentVisitor {

    private final EditorWindow editorWindow;

    public CreateComponentConnectionsResolver(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    @Override
    public void visit(Scene scene) {
        final AsisConnectionButton output = editorWindow.getNodeManager().getJOIComponentNodeWithId(scene.getComponentId()).getOutputButtons().get(0);
        if (scene.hasComponent(GotoScene.class))
            createConnections(scene.getComponent(GotoScene.class), output);

        createConnectionsForDialogOutputs(scene);
    }

    @Override
    public void visit(Condition condition) {
        final AsisConnectionButton trueOutput = editorWindow.getNodeManager().getJOIComponentNodeWithId(condition.getComponentId()).getOutputButtons().get(0);
        final AsisConnectionButton falseOutput = editorWindow.getNodeManager().getJOIComponentNodeWithId(condition.getComponentId()).getOutputButtons().get(1);

        createConnections(condition.getGotoSceneTrue(), trueOutput);
        createConnections(condition.getGotoSceneFalse(), falseOutput);
    }

    @Override
    public void visit(VariableSetter variableSetter) {
        final AsisConnectionButton output = editorWindow.getNodeManager().getJOIComponentNodeWithId(variableSetter.getComponentId()).getOutputButtons().get(0);
        createConnections(variableSetter.getGotoScene(), output);
    }

    @Override
    public void visit(Arithmetic arithmetic) {
        final AsisConnectionButton output = editorWindow.getNodeManager().getJOIComponentNodeWithId(arithmetic.getComponentId()).getOutputButtons().get(0);
        createConnections(arithmetic.getGotoScene(), output);
    }

    @Override
    public void visit(Group group) {
        //final AsisConnectionButton output = editorWindow.getNodeManager().getJOIComponentNodeWithId(group.getComponentId()).getOutputButtons().get(0);
        //createConnections(group.getGotoScene(), output);
    }

    private void createConnectionsForDialogOutputs(Scene scene) {
        if (scene.hasComponent(Dialog.class) && !scene.getComponent(Dialog.class).getOptionArrayList().isEmpty()) {
            for (DialogOption dialogOption : scene.getComponent(Dialog.class).getOptionArrayList()) {
                AsisConnectionButton output = editorWindow.getNodeManager().getJOIComponentNodeWithId(scene.getComponentId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    private void createConnections(GotoScene gotoScene, AsisConnectionButton output) {
        final boolean gotoHasSingleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() == 1;
        final boolean gotoHasMultipleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() > 1;

        try {
            ComponentConnectionManager componentConnectionManager = editorWindow.getConnectionManager();

            //Check for scene normal connections
            if (gotoHasSingleOutput) {
                AsisConnectionButton input = editorWindow.getNodeManager().getJOIComponentNodeWithId(gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
                componentConnectionManager.createConnection(output, input);
            }

            //Check for scene range connections
            if (gotoHasMultipleOutput) {
                for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                    AsisConnectionButton input = editorWindow.getNodeManager().getJOIComponentNodeWithId(gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                    componentConnectionManager.createConnection(output, input);
                }
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load scene: " + output.getJoiComponent().getComponentTitle());
        }
    }

}
