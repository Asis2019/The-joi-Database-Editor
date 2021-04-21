package com.asis.ui.asis_node.node_group;

import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.GroupBridge;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.ui.asis_node.JOIComponentNode;

/**
 * This class adds a way for nodes inside a group to be piped to the group node itself.
 */
public class NodeGroupBridge extends JOIComponentNode {

    public NodeGroupBridge(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(150, 0, componentId, component, editorWindow);

        setUserData("group_bridge");
        setId("NodeGroupBridge");

        if(((GroupBridge) getJoiComponent()).isInputBridge()) {
            createNewOutputConnectionPoint("group input", "normal_output");
        } else {
            createNewInputConnectionPoint("group output");
        }

    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle("-fx-background-color: #5a5a5a;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-insets: 8;" +
                    "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle("-fx-background-color: #5a5a5a;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-insets: 8;" +
                    "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    @Override
    protected boolean openDialog() {
        return true;
    }

}
