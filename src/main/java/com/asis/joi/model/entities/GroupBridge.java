package com.asis.joi.model.entities;

import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import org.json.JSONArray;
import org.json.JSONObject;

public class GroupBridge extends JOIComponent {

    private GotoScene gotoScene;
    private boolean isInputBridge = false;

    public GroupBridge(int componentId) {
        super(componentId);
        setComponentTitle("");
    }

    public static GroupBridge createEntity(JSONObject jsonObject) {
        GroupBridge groupBridge = new GroupBridge(0);
        createEntity(jsonObject, groupBridge);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "isInputBridge":
                    groupBridge.setInputBridge(jsonObject.getBoolean(key));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRangeObject = new JSONObject();
                    gotoRangeObject.put("array", jsonObject.getJSONArray(key));

                    groupBridge.setGotoScene(GotoScene.createEntity(gotoRangeObject));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt(key)}));

                    groupBridge.setGotoScene(GotoScene.createEntity(gotoObject));
                    break;
            }
        }

        return groupBridge;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("componentType", "NodeGroupBridge");
        jsonObject.put("isInputBridge", isInputBridge());
        if(getGotoScene() != null)  jsonObject.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GroupBridge groupBridge = (GroupBridge) super.clone();

        groupBridge.setGotoScene(getGotoScene());
        groupBridge.setInputBridge(isInputBridge());

        return groupBridge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupBridge)) return false;
        if (!super.equals(o)) return false;

        GroupBridge that = (GroupBridge) o;

        if (isInputBridge() != that.isInputBridge()) return false;
        return getGotoScene() != null ? getGotoScene().equals(that.getGotoScene()) : that.getGotoScene() == null;
    }

    @Override
    public void accept(ComponentVisitor componentVisitor) {
        componentVisitor.visit(this);
    }

    //Getters and setters
    public GotoScene getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }

    public boolean isInputBridge() {
        return isInputBridge;
    }
    public void setInputBridge(boolean inputBridge) {
        isInputBridge = inputBridge;
    }
}
