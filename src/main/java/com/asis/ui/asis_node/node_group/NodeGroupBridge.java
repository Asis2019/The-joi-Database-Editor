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
    protected boolean openDialog() {
        return true;
    }

}
