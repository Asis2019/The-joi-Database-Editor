package com.asis.joi.model.entities;

import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import org.json.JSONArray;
import org.json.JSONObject;

public class Group extends JOIComponent {
    private GroupBridge inputNodeData, outputNodeData;
    private GotoScene gotoScene;

    public Group(int componentId) {
        super(componentId);
    }

    public static Group createEntity(JSONObject jsonObject) {
        Group group = new Group(0);
        createEntity(jsonObject, group);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "gotoSceneInRange":
                    JSONObject gotoRangeObject = new JSONObject();
                    gotoRangeObject.put("array", jsonObject.getJSONArray(key));

                    group.setGotoScene(GotoScene.createEntity(gotoRangeObject));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt(key)}));

                    group.setGotoScene(GotoScene.createEntity(gotoObject));
                    break;
            }
        }

        return group;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("componentType", "NodeGroup");
        jsonObject.put("inputBridge", getInputNodeData().getComponentId());
        if(getGotoScene() != null)  jsonObject.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Group group = (Group) super.clone();

        group.setGotoScene(getGotoScene());
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

        if (getInputNodeData() != null ? !getInputNodeData().equals(group.getInputNodeData()) : group.getInputNodeData() != null)
            return false;
        if (getOutputNodeData() != null ? !getOutputNodeData().equals(group.getOutputNodeData()) : group.getOutputNodeData() != null)
            return false;
        return getGotoScene() != null ? getGotoScene().equals(group.getGotoScene()) : group.getGotoScene() == null;
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

    public GotoScene getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }
}
