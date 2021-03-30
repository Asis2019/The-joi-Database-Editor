package com.asis.joi.model.entities;

import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import org.json.JSONObject;

public class Group extends JOIComponent {
    private GroupBridge inputNodeData;
    private GroupBridge outputNodeData;

    public Group(int componentId) {
        super(componentId);
    }

    public static Group createEntity(JSONObject jsonObject) {
        Group group = new Group(0);
        createEntity(jsonObject, group);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "inputNodeData":
                    group.setInputNodeData(GroupBridge.createEntity(jsonObject.getJSONObject(key)));
                    break;
                case "outputNodeData":
                    group.setOutputNodeData(GroupBridge.createEntity(jsonObject.getJSONObject(key)));
                    break;
            }
        }

        return group;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("componentType", "NodeGroup");
        jsonObject.put("inputNodeData", getInputNodeData().toJSON());
        jsonObject.put("outputNodeData", getOutputNodeData().toJSON());

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Group group = (Group) super.clone();

        group.setInputNodeData(getInputNodeData());
        group.setOutputNodeData(getOutputNodeData());

        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        if (!super.equals(o)) return false;

        Group group = (Group) o;

        if (!getInputNodeData().equals(group.getInputNodeData())) return false;
        return getOutputNodeData().equals(group.getOutputNodeData());
    }

    @Override
    public void accept(ComponentVisitor componentVisitor) {
        componentVisitor.visit(this);
    }

    public GroupBridge getInputNodeData() {
        return inputNodeData;
    }

    public GroupBridge getOutputNodeData() {
        return outputNodeData;
    }

    public void setInputNodeData(GroupBridge inputNodeData) {
        this.inputNodeData = inputNodeData;
    }

    public void setOutputNodeData(GroupBridge outputNodeData) {
        this.outputNodeData = outputNodeData;
    }
}
