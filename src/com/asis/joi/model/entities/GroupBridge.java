package com.asis.joi.model.entities;

import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.asis.utilities.AsisUtils.convertJSONArrayToList;

public class GroupBridge extends JOIComponent {

    private ArrayList<Integer> connectedScenes = new ArrayList<>();

    public GroupBridge(int componentId) {
        super(componentId);
    }

    public static GroupBridge createEntity(JSONObject jsonObject) {
        GroupBridge groupBridge = new GroupBridge(0);
        createEntity(jsonObject, groupBridge);

        for (String key : jsonObject.keySet()) {
            if ("inputNodeData".equals(key)) {
                groupBridge.setConnectedScenes(convertJSONArrayToList(jsonObject.getJSONArray(key)));
                break;
            }
        }

        //group.getInputNodeData().layoutYPosition = 30;
        //group.getOutputNodeData().layoutYPosition = 30;
        //group.getOutputNodeData().layoutXPosition = 400;

        return groupBridge;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("connectedScenes", connectedScenes);

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GroupBridge groupBridge = (GroupBridge) super.clone();

        groupBridge.setConnectedScenes(new ArrayList<>(getConnectedScenes()));

        return groupBridge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupBridge)) return false;
        if (!super.equals(o)) return false;

        GroupBridge that = (GroupBridge) o;

        return getConnectedScenes().equals(that.getConnectedScenes());
    }

    @Override
    public void accept(ComponentVisitor componentVisitor) {

    }

    //Getters and setters

    public ArrayList<Integer> getConnectedScenes() {
        return connectedScenes;
    }
    public void setConnectedScenes(ArrayList<Integer> connectedScenes) {
        this.connectedScenes = connectedScenes;
    }
}
