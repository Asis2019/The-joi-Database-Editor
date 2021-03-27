package com.asis.joi;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogMessage;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.*;
import com.asis.ui.asis_node.node_group.NodeGroup;
import com.asis.utilities.StageManager;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LoadJOIService {

    private static final LoadJOIService LOAD_JOI_SERVICE = new LoadJOIService();
    private JOIPackage joiPackage;

    private LoadJOIService(){}

    public static LoadJOIService getInstance() {
        return LOAD_JOI_SERVICE;
    }

    public boolean processLoadProject(InfinityPane infinityPane) throws IOException {
        boolean result = processLoadPackage();
        if(result) addNodesToPane(infinityPane);
        return result;
    }

    public boolean processLoadPackage() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        File file = directoryChooser.showDialog(null);

        if (file != null) {
            try {
                //Set project directory to current and load joi package
                JOIPackageManager.getInstance().setJoiPackageDirectory(file);
                JOIPackage newJoiPackage = JOIPackageManager.getInstance().getJOIPackage();
                if (newJoiPackage == null) return false;

                //Reset old variables and main pane
                setJoiPackage(newJoiPackage);

                // Clear all nodes from main pane
                Controller.getInstance().getInfinityPane().getContainer().getChildren().clear();

                getJoiComponentNodes().clear();
                StageManager.getInstance().closeAllStages();

                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                DialogMessage.messageDialog("LOADING FAILED", "The editor was unable to load this joi for the following reason:\n" + e.getMessage(), 600, 200);
                return false;
            }
        }

        //Loading failed do to null file
        return false;
    }

    public void addNodesToPane(InfinityPane infinityPane, int... groupId) {
        //Set the pane
        ComponentNodeManager.getInstance().setWorkingPane(infinityPane);

        //Create nodes for all joi components
        for (JOIComponent component : getJoiPackage().getJoi().getJoiComponents()) {
            // Check if component is in the requested load group
            if(groupId.length >= 1 && component.getGroupId() != groupId[0]) continue;

            if (component instanceof com.asis.joi.model.entities.Scene)
                ComponentNodeManager.getInstance().addJOIComponentNode(
                        SceneNode.class, com.asis.joi.model.entities.Scene.class,
                        component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(),
                        component.getComponentId(), true);

            else if (component instanceof VariableSetter)
                ComponentNodeManager.getInstance().addJOIComponentNode(
                        VariableSetterNode.class, VariableSetter.class,
                        component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);

            else if (component instanceof Condition)
                ComponentNodeManager.getInstance().addJOIComponentNode(
                        ConditionNode.class, Condition.class,
                        component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);

            else if (component instanceof Arithmetic)
                ComponentNodeManager.getInstance().addJOIComponentNode(
                        ArithmeticNode.class, Arithmetic.class,
                        component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);

            else if (component instanceof Group)
                ComponentNodeManager.getInstance().addJOIComponentNode(
                        NodeGroup.class, Group.class,
                        component.getLayoutXPosition(), component.getLayoutYPosition(), component.getComponentTitle(), component.getComponentId(), true);
        }

        //Create the connections for the nodes
        for (JOIComponent component : getJoiPackage().getJoi().getJoiComponents()) {
            if (component instanceof com.asis.joi.model.entities.Scene) {
                com.asis.joi.model.entities.Scene scene = (com.asis.joi.model.entities.Scene) component;
                final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                if (scene.hasComponent(GotoScene.class))
                    createConnections(scene.getComponent(GotoScene.class), output);

                createConnectionsForDialogOutputs(scene);
            } else if (component instanceof VariableSetter) {
                VariableSetter setter = (VariableSetter) component;
                final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                createConnections(setter.getGotoScene(), output);
            } else if (component instanceof Condition) {
                Condition condition = (Condition) component;
                final AsisConnectionButton trueOutput = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);
                final AsisConnectionButton falseOutput = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(1);

                createConnections(condition.getGotoSceneTrue(), trueOutput);
                createConnections(condition.getGotoSceneFalse(), falseOutput);
            } else if (component instanceof Arithmetic) {
                Arithmetic arithmetic = (Arithmetic) component;
                final AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), component.getComponentId()).getOutputButtons().get(0);

                createConnections(arithmetic.getGotoScene(), output);
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
                AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(0)).getInputConnection();
                componentConnectionManager.createConnection(output, input);
            }

            //Check for scene range connections
            if (gotoHasMultipleOutput) {
                for (int i = 0; i < gotoScene.getGotoSceneArrayList().size(); i++) {
                    AsisConnectionButton input = getJOIComponentNodeWithId(getJoiComponentNodes(), gotoScene.getGotoSceneArrayList().get(i)).getInputConnection();
                    componentConnectionManager.createConnection(output, input);
                }
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load scene: " + output.getJoiComponent().getComponentTitle());
        }
    }

    public JOIComponentNode getJOIComponentNodeWithId(ArrayList<JOIComponentNode> components, int componentId) {
        for (JOIComponentNode componentNode : components)
            if (componentNode.getComponentId() == componentId) return componentNode;
        return null;
    }

    private void createConnectionsForDialogOutputs(com.asis.joi.model.entities.Scene scene) {
        if (scene.hasComponent(Dialog.class) && !scene.getComponent(Dialog.class).getOptionArrayList().isEmpty()) {
            for (DialogOption dialogOption : scene.getComponent(Dialog.class).getOptionArrayList()) {
                AsisConnectionButton output = getJOIComponentNodeWithId(getJoiComponentNodes(), scene.getComponentId()).createNewOutputConnectionPoint("Option " + dialogOption.getOptionNumber(), "dialog_option_" + (dialogOption.getOptionNumber() + 1));
                output.setOptionNumber(dialogOption.getOptionNumber());

                createConnections(dialogOption.getGotoScene(), output);
            }
        }
    }

    //Getters and setters
    private ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return ComponentNodeManager.getInstance().getJoiComponentNodes();
    }

    public JOIPackage getJoiPackage() {
        return joiPackage;
    }
    public void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }
}
