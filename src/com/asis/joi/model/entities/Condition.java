package com.asis.joi.model.entities;

import org.json.JSONArray;
import org.json.JSONObject;

public class Condition extends JOIComponent {
    public enum ConditionType {
        EQUALS,
        GREATER_THAN,
        LESS_THAN
    }

    private GotoScene gotoSceneTrue;
    private GotoScene gotoSceneFalse;
    private String variable;
    private Object comparingValue;
    private ConditionType conditionType = ConditionType.EQUALS;

    public Condition(int componentId) {
        super(componentId);
    }

    public static Condition createEntity(JSONObject jsonObject) {
        Condition condition = new Condition(0);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "sceneId":
                    condition.setComponentId(jsonObject.getInt("sceneId"));
                    break;
                case "sceneTitle":
                    condition.setComponentTitle(jsonObject.getString("sceneTitle"));
                    break;
                case "layoutXPosition":
                    condition.setLayoutXPosition(jsonObject.getDouble("layoutXPosition"));
                    break;
                case "layoutYPosition":
                    condition.setLayoutYPosition(jsonObject.getDouble("layoutYPosition"));
                    break;
                case "conditionType":
                    condition.setConditionType(ConditionType.valueOf(jsonObject.getString("conditionType")));
                    break;
                case "variable":
                    condition.setVariable(jsonObject.getString("variable"));
                    break;
                case "comparingValue":
                    condition.setComparingValue(jsonObject.get("comparingValue"));
                    break;
                case "gotoScene":
                    JSONArray jsonArray = jsonObject.getJSONArray("gotoScene");
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject gotoObject = new JSONObject();

                        if(jsonArray.getJSONObject(i).has("gotoScene")) {
                            gotoObject.put("array", new JSONArray(new int[]{jsonArray.getJSONObject(i).getInt("gotoScene")}));
                        } else if(jsonArray.getJSONObject(i).has("gotoSceneInRange")) {
                            gotoObject.put("array", jsonArray.getJSONObject(i).getJSONArray("gotoSceneInRange"));
                        }

                        if(jsonArray.getJSONObject(i).getBoolean("output")) {
                            condition.setGotoSceneTrue(GotoScene.createEntity(gotoObject));
                        } else {
                            condition.setGotoSceneFalse(GotoScene.createEntity(gotoObject));
                        }
                    }
                    break;
            }
        }

        return condition;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = super.toJSON();
        object.put("conditionType", getConditionType());

        if(getVariable() != null) object.put("variable", getVariable());
        if(getComparingValue() != null) object.put("comparingValue", getComparingValue());

        object.put("componentType", "Conditional");

        JSONArray outputArray = new JSONArray();
        if(getGotoSceneFalse() != null) {
            JSONObject outputObject = new JSONObject();
            outputObject.put("output", false);
            outputObject.put(getGotoSceneFalse().getJsonKeyName(), getGotoSceneFalse().getJsonValue());
            outputArray.put(outputObject);
        }
        if(getGotoSceneTrue() != null) {
            JSONObject outputObject = new JSONObject();
            outputObject.put("output", true);
            outputObject.put(getGotoSceneTrue().getJsonKeyName(), getGotoSceneTrue().getJsonValue());
            outputArray.put(outputObject);
        }

        object.put("gotoScene", outputArray);

        return object;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Condition condition = (Condition) super.clone();

        condition.setConditionType(getConditionType());
        condition.setVariable(getVariable());
        condition.setComparingValue(getComparingValue());

        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;
        if (!super.equals(o)) return false;

        Condition condition = (Condition) o;

        if (getGotoSceneTrue() != null ? !getGotoSceneTrue().equals(condition.getGotoSceneTrue()) : condition.getGotoSceneTrue() != null)
            return false;
        if (getGotoSceneFalse() != null ? !getGotoSceneFalse().equals(condition.getGotoSceneFalse()) : condition.getGotoSceneFalse() != null)
            return false;
        if (getVariable() != null ? !getVariable().equals(condition.getVariable()) : condition.getVariable() != null)
            return false;
        if (getComparingValue() != null ? !getComparingValue().equals(condition.getComparingValue()) : condition.getComparingValue() != null)
            return false;
        return getConditionType() == condition.getConditionType();
    }

    @Override
    public int hashCode() {
        int result = getGotoSceneTrue() != null ? getGotoSceneTrue().hashCode() : 0;
        result = 31 * result + (getGotoSceneFalse() != null ? getGotoSceneFalse().hashCode() : 0);
        result = 31 * result + (getVariable() != null ? getVariable().hashCode() : 0);
        result = 31 * result + (getComparingValue() != null ? getComparingValue().hashCode() : 0);
        result = 31 * result + getConditionType().hashCode();
        return result;
    }

    //Getters and Setters
    public GotoScene getGotoSceneTrue() {
        return gotoSceneTrue;
    }
    public void setGotoSceneTrue(GotoScene gotoSceneTrue) {
        this.gotoSceneTrue = gotoSceneTrue;
    }

    public GotoScene getGotoSceneFalse() {
        return gotoSceneFalse;
    }
    public void setGotoSceneFalse(GotoScene gotoSceneFalse) {
        this.gotoSceneFalse = gotoSceneFalse;
    }

    public String getVariable() {
        return variable;
    }
    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Object getComparingValue() {
        return comparingValue;
    }
    public void setComparingValue(Object comparingValue) {
        this.comparingValue = comparingValue;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }
    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
}