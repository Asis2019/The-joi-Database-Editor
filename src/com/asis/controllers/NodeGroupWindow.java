package com.asis.controllers;

import com.asis.controllers.dialogs.DialogMessage;
import com.asis.joi.model.entities.Group;
import com.asis.joi.model.entities.GroupBridge;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.node_functional_expansion.AddComponentNodeResolver;
import com.asis.ui.asis_node.node_functional_expansion.CreateComponentConnectionsResolver;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class NodeGroupWindow extends EditorWindow {

    private Group group;

    @FXML
    private InfinityPane infinityPane;

    public void initialize(Group group) {
        this.group = group;

        loadNodes();
    }

    private void loadNodes() {
        try {
            resetEditorWindow();

            //Create the input/output bridging nodes if needed
            loadGroupInternals();

            //Create component nodes
            for (JOIComponent component : Controller.getInstance().getJoiPackage().getJoi().getJoiComponents()) {
                if (component.getGroupId() != getGroup().getComponentId()) continue;

                component.accept(new AddComponentNodeResolver(this));
            }

            //Create connections
            for (JOIComponent component : Controller.getInstance().getJoiPackage().getJoi().getJoiComponents()) {
                if (component.getGroupId() != getGroup().getComponentId()) continue;

                component.accept(new CreateComponentConnectionsResolver(this));
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
            DialogMessage.messageDialog("LOADING FAILED", "The editor was unable to load this group for the following reason:\n" + e.getMessage(), 600, 200);
        }
    }

    private void loadGroupInternals() {
        if (getGroup().getInputNodeData() == null) {
            final int componentId = Controller.getInstance().getJoiPackage().getJoi().getSceneIdCounter() + 1;

            Controller.getInstance().getJoiPackage().getJoi().addNewComponent(GroupBridge.class, componentId);
            GroupBridge groupBridge = (GroupBridge) Controller.getInstance().getJoiPackage().getJoi().getComponent(componentId);

            groupBridge.setInputBridge(true);
            groupBridge.setGroupId(getGroup().getComponentId());
            getGroup().setInputNodeData(groupBridge);
        }

        if (getGroup().getOutputNodeData() == null) {
            final int componentId = Controller.getInstance().getJoiPackage().getJoi().getSceneIdCounter() + 1;

            Controller.getInstance().getJoiPackage().getJoi().addNewComponent(GroupBridge.class, componentId);
            GroupBridge groupBridge = (GroupBridge) Controller.getInstance().getJoiPackage().getJoi().getComponent(componentId);

            groupBridge.setLayoutXPosition(400);
            groupBridge.setInputBridge(false);
            groupBridge.setGroupId(getGroup().getComponentId());
            getGroup().setOutputNodeData(groupBridge);
        }
    }

    @FXML
    private void actionClose() {
        Stage stage = (Stage) getInfinityPane().getScene().getWindow();
        StageManager.getInstance().closeStage(stage);
    }

    // Getters and setters
    @Override
    public InfinityPane getInfinityPane() {
        return infinityPane;
    }

    public Group getGroup() {
        return group;
    }
}
