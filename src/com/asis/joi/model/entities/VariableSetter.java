package com.asis.joi.model.entities;

import org.json.JSONArray;
import org.json.JSONObject;

public class VariableSetter extends JOIComponent {
    private String variableName;
    private Object variableValue;
    private boolean isVariablePersistent = false;
    private GotoScene gotoScene;

    public VariableSetter(int componentId) {
        super(componentId);
    }

    public static VariableSetter createEntity(JSONObject jsonObject) {
        VariableSetter setter = new VariableSetter(0);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "sceneId":
                    setter.setComponentId(jsonObject.getInt("sceneId"));
                    break;
                case "sceneTitle":
                    setter.setComponentTitle(jsonObject.getString("sceneTitle"));
                    break;
                case "layoutXPosition":
                    setter.setLayoutXPosition(jsonObject.getDouble("layoutXPosition"));
                    break;
                case "layoutYPosition":
                    setter.setLayoutYPosition(jsonObject.getDouble("layoutYPosition"));
                    break;
                case "name":
                    setter.setVariableName(jsonObject.getString("name"));
                    break;
                case "value":
                    setter.setVariableValue(jsonObject.get("value"));
                    break;
                case "persistent":
                    setter.setVariablePersistent(jsonObject.getBoolean("persistent"));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRangeObject = new JSONObject();
                    gotoRangeObject.put("array", jsonObject.getJSONArray("gotoSceneInRange"));

                    setter.setGotoScene(GotoScene.createEntity(gotoRangeObject));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt("gotoScene")}));

                    setter.setGotoScene(GotoScene.createEntity(gotoObject));
                    break;
            }
        }

        return setter;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = super.toJSON();
        object.put("name", getVariableName());
        object.put("value", getVariableValue());
        object.put("persistent", isVariablePersistent());
        object.put("componentType", "VariableSetter");
        if(getGotoScene() != null)  object.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        return object;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        VariableSetter variableSetter = (VariableSetter) super.clone();

        variableSetter.setVariableName(getVariableName());
        variableSetter.setVariablePersistent(isVariablePersistent());
        variableSetter.setVariableValue(getVariableValue());
        //TODO might need to add gotoscene

        return variableSetter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableSetter)) return false;
        if (!super.equals(o)) return false;

        VariableSetter that = (VariableSetter) o;

        if (isVariablePersistent() != that.isVariablePersistent()) return false;
        if (getVariableName() != null ? !getVariableName().equals(that.getVariableName()) : that.getVariableName() != null)
            return false;
        if (getVariableValue() != null ? !getVariableValue().equals(that.getVariableValue()) : that.getVariableValue() != null)
            return false;
        return getGotoScene() != null ? getGotoScene().equals(that.getGotoScene()) : that.getGotoScene() == null;
    }

    //Getters and Setters
    public String getVariableName() {
        return variableName;
    }
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getVariableValue() {
        return variableValue;
    }
    public void setVariableValue(Object variableValue) {
        this.variableValue = variableValue;
    }

    public boolean isVariablePersistent() {
        return isVariablePersistent;
    }
    public void setVariablePersistent(boolean variablePersistent) {
        isVariablePersistent = variablePersistent;
    }

    public GotoScene getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }
}
