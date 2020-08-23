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
    private String firstVariable, secondVariable;
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
                case "variable1":
                    condition.setFirstVariable(jsonObject.getString("variable1"));
                    break;
                case "variable2":
                    condition.setSecondVariable(jsonObject.getString("variable2"));
                    break;
                /*case "gotoSceneInRange":
                    JSONObject gotoRangeObject = new JSONObject();
                    //gotoRangeObject.put("array", jsonObject.getJSONArray("gotoSceneInRange"));

                    //condition.setGotoScene(GotoScene.createEntity(gotoRangeObject));
                    break;*/
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

        if(getFirstVariable() != null) object.put("variable1", getFirstVariable());
        if(getSecondVariable() != null) object.put("variable2", getSecondVariable());

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
        condition.setFirstVariable(getFirstVariable());
        condition.setSecondVariable(getSecondVariable());

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
        if (getFirstVariable() != null ? !getFirstVariable().equals(condition.getFirstVariable()) : condition.getFirstVariable() != null)
            return false;
        if (getSecondVariable() != null ? !getSecondVariable().equals(condition.getSecondVariable()) : condition.getSecondVariable() != null)
            return false;
        return getConditionType() == condition.getConditionType();
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

    public String getFirstVariable() {
        return firstVariable;
    }
    public void setFirstVariable(String firstVariable) {
        this.firstVariable = firstVariable;
    }

    public String getSecondVariable() {
        return secondVariable;
    }
    public void setSecondVariable(String secondVariable) {
        this.secondVariable = secondVariable;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }
    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
}
