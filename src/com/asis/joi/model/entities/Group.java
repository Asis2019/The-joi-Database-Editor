package com.asis.joi.model.entities;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.asis.utilities.AsisUtils.convertJSONArrayToList;

public class Group extends JOIComponent {
    private final InnerNode inputNodeData = new InnerNode();
    private final InnerNode outputNodeData = new InnerNode();

    public Group(int componentId) {
        super(componentId);
    }

    public static Group createEntity(JSONObject jsonObject) {
        Group group = new Group(0);
        createEntity(jsonObject, group);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "inputNodeData":
                    group.getInputNodeData().fromJSON(jsonObject.getJSONObject(key));
                    break;
                case "outputNodeData":
                    group.getOutputNodeData().fromJSON(jsonObject.getJSONObject(key));
                    break;
            }
        }

        return group;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("componentType", "NodeGroup");
        jsonObject.put("inputNodeData", inputNodeData.toJSON());
        jsonObject.put("outputNodeData", outputNodeData.toJSON());

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Group group = (Group) super.clone();

        setInnerNode(getInputNodeData(), group.getInputNodeData());
        setInnerNode(getOutputNodeData(), group.getOutputNodeData());

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

    public InnerNode getInputNodeData() {
        return inputNodeData;
    }
    public InnerNode getOutputNodeData() {
        return outputNodeData;
    }

    private void setInnerNode(InnerNode fromNode, InnerNode toNode) {
        fromNode.connectedScenes = toNode.connectedScenes;
        fromNode.layoutXPosition = toNode.layoutXPosition;
        fromNode.layoutYPosition = toNode.layoutYPosition;
    }

    private static class InnerNode {
        public ArrayList<Integer> connectedScenes = new ArrayList<>();
        public int layoutXPosition = 0, layoutYPosition = 0;

        public JSONObject toJSON() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("layoutXPosition", layoutXPosition);
            jsonObject.put("layoutYPosition", layoutYPosition);
            jsonObject.put("connectedScenes", connectedScenes);

            return jsonObject;
        }

        public void fromJSON(JSONObject jsonObject) {
            for (String key : jsonObject.keySet()) {
                switch (key) {
                    case "layoutXPosition":
                        layoutXPosition = jsonObject.getInt(key);
                        break;
                    case "layoutYPosition":
                        layoutYPosition = jsonObject.getInt(key);
                        break;
                    case "connectedScenes":
                        connectedScenes = convertJSONArrayToList(jsonObject.getJSONArray(key));
                        break;

                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InnerNode)) return false;

            InnerNode innerNode = (InnerNode) o;

            if (layoutXPosition != innerNode.layoutXPosition) return false;
            if (layoutYPosition != innerNode.layoutYPosition) return false;
            return connectedScenes.equals(innerNode.connectedScenes);
        }
    }
}
