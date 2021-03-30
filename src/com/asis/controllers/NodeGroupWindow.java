package com.asis.controllers;

import com.asis.controllers.dialogs.DialogMessage;
import com.asis.joi.model.entities.Group;
import com.asis.joi.model.entities.GroupBridge;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.node_functional_expansion.AddComponentNodeResolver;
import com.asis.ui.asis_node.node_functional_expansion.CreateComponentConnectionsResolver;
import com.asis.ui.asis_node.node_group.NodeGroupBridge;
import com.asis.utilities.Config;
import com.asis.utilities.Draggable;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import org.json.JSONObject;

public class NodeGroupWindow extends EditorWindow {

    private Group group;

    @FXML
    private InfinityPane infinityPane;
    @FXML
    public MenuBar mainMenuBar;

    public void initialize(Group group) {
        this.group = group;

        try {
            JSONObject object = (JSONObject) Config.get("ZOOM");
            if (object.has("minimum")) getInfinityPane().setMinimumScale(object.getDouble("minimum"));
            if (object.has("maximum")) getInfinityPane().setMaximumScale(object.getDouble("maximum"));
        } catch (ClassCastException ignore) {
        }

        loadNodes();
    }

    private void loadNodes() {
        try {
            resetEditorWindow();

            //Load the input/output bridging nodes
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
            DialogMessage.messageDialog("LOADING FAILED", "The editor was unable to load this joi for the following reason:\n" + e.getMessage(), 600, 200);
        }
    }

    private void loadGroupInternals() {
        if(getGroup().getInputNodeData() == null) {
            final int sceneId = Controller.getInstance().getJoiPackage().getJoi().getSceneIdCounter() + 1;

            GroupBridge groupBridge = new GroupBridge(sceneId);
            addNodeGroupBridgeToPane(0, 30, "Input", groupBridge, true);

            getGroup().setInputNodeData(groupBridge);
        } else {
            addNodeGroupBridgeToPane(getGroup().getInputNodeData().getLayoutXPosition(),
                    getGroup().getInputNodeData().getLayoutYPosition(), "Input", getGroup().getInputNodeData(), true);
        }

        if(getGroup().getOutputNodeData() == null) {
            final int sceneId = Controller.getInstance().getJoiPackage().getJoi().getSceneIdCounter() + 1;

            GroupBridge groupBridge = new GroupBridge(sceneId);
            addNodeGroupBridgeToPane(400, 30, "Output", groupBridge, false);

            getGroup().setOutputNodeData(groupBridge);
        } else {
            addNodeGroupBridgeToPane(getGroup().getOutputNodeData().getLayoutXPosition(),
                    getGroup().getOutputNodeData().getLayoutYPosition(), "Output", getGroup().getOutputNodeData(), false);
        }

    }

    private void addNodeGroupBridgeToPane(double xPosition, double yPosition, String title, GroupBridge component, boolean isInput) {
        NodeGroupBridge componentNode = new NodeGroupBridge(100, 150, -100, component, this);

        if(component.getConnectedScenes().isEmpty()) {
            if (isInput) {
                componentNode.createNewOutputConnectionPoint("out", "01");
            } else {
                componentNode.createNewInputConnectionPoint("in");
            }
        } else {
            component.getConnectedScenes().forEach(integer -> {
                if (isInput) {
                    componentNode.createNewOutputConnectionPoint("out", "01");
                } else {
                    componentNode.createNewInputConnectionPoint("in");
                }
            });
        }


        new Draggable.Nature(componentNode);
        componentNode.setTitle(title);

        componentNode.positionInGrid(xPosition, yPosition);

        componentNode.toBack();
        getInfinityPane().getContainer().getChildren().add(componentNode);

    }

    @FXML
    private void actionClose() {
        Stage stage = (Stage) getInfinityPane().getScene().getWindow();
        StageManager.getInstance().closeStage(stage);
    }

    // Getters and setters
    public InfinityPane getInfinityPane() {
        return infinityPane;
    }

    public Group getGroup() {
        return group;
    }
}
