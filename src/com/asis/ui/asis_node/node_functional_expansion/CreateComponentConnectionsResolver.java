package com.asis.ui.asis_node.node_functional_expansion;

import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.ComponentConnectionManager;

import static com.asis.ui.asis_node.ComponentNodeManager.getJOIComponentNodeWithId;

/**
 * This class is used to create connections with ComponentConnectionManager between components.
 */
public class CreateComponentConnectionsResolver implements ComponentVisitor {

    @Override
    public void visit(Scene scene) {
        final AsisConnectionButton output = getJOIComponentNodeWithId(scene.getComponentId()).getOutputButtons().get(0);
        if (scene.hasComponent(GotoScene.class))
            createConnections(scene.getComponent(GotoScene.class), output);

        createConnectionsForDialogOutputs(scene);
    }

    @Override
    public void visit(Condition condition) {
        final AsisConnectionButton trueOutput = getJOIComponentNodeWithId(condition.getComponentId()).getOutputButtons().get(0);
        final AsisConnectionButton falseOutput = getJOIComponentNodeWithId(condition.getComponentId()).getOutputButtons().get(1);

        createConnections(condition.getGotoSceneTrue(), trueOutput);
        createConnections(condition.getGotoSceneFalse(), falseOutput);
    }

    @Override
    public void visit(VariableSetter variableSetter) {
        final AsisConnectionButton output = getJOIComponentNodeWithId(variableSetter.getComponentId()).getOutputButtons().get(0);
        createConnections(variableSetter.getGotoScene(), output);
    }

    @Override
    public void visit(Arithmetic arithmetic) {
        final AsisConnectionButton output = getJOIComponentNodeWithId(arithmetic.getComponentId()).getOutputButtons().get(0);

        createConnections(arithmetic.getGotoScene(), output);
    }

    private void createConnectionsForDialogOutputs(Scene scene) {
        if (scene.hasComponent(Dialog.class) && !scene.getComponent(Dialog.class).getOptionArrayList().isEmpty()) {
            for (DialogOption dialogOption : scene.getComponent(Dialog.class).getOptionArrayList()) {
                AsisConnectionButton output = getJOIComponentNodeWithId(scene.getComponentId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    private void createConnections(GotoScene gotoScene, AsisConnectionButton output) {
        final boolean gotoHasSingleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() == 1;
        final boolean gotoHasMultipleOutput = gotoScene != null && gotoScene.getGotoSceneArrayList().size() > 1;

        try {
            ComponentConnectionManager componentConnectionManager = ComponentConnectionManager.getInstance();

            //Check for scene normal connections
            if (gotoHasSingleOutput) {
                AsisConnectionButton input = getJOIComponentNodeWithId(gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
                componentConnectionManager.createConnection(output, input);
            }

            //Check for scene range connections
            if (gotoHasMultipleOutput) {
                for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                    AsisConnectionButton input = getJOIComponentNodeWithId(gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                    componentConnectionManager.createConnection(output, input);
                }
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load scene: " + output.getJoiComponent().getComponentTitle());
        }
    }

}
