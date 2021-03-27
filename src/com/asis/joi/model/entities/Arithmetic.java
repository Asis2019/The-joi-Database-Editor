package com.asis.joi.model.entities;

import org.json.JSONArray;
import org.json.JSONObject;

public class Arithmetic extends JOIComponent {
    private GotoScene gotoScene;
    private String mathematicalExpression = "";

    public Arithmetic(int componentId) {
        super(componentId);
    }

    public static Arithmetic createEntity(JSONObject jsonObject) {
        Arithmetic arithmetic = new Arithmetic(0);
        createEntity(jsonObject, arithmetic);

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "operation":
                    arithmetic.setMathematicalExpression(jsonObject.getString(key));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRangeObject = new JSONObject();
                    gotoRangeObject.put("array", jsonObject.getJSONArray(key));

                    arithmetic.setGotoScene(GotoScene.createEntity(gotoRangeObject));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt(key)}));

                    arithmetic.setGotoScene(GotoScene.createEntity(gotoObject));
                    break;
            }
        }

        return arithmetic;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();

        jsonObject.put("componentType", "Arithmetic");
        jsonObject.put("operation", getMathematicalExpression());
        if(getGotoScene() != null)  jsonObject.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        return jsonObject;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Arithmetic arithmetic = (Arithmetic) super.clone();

        arithmetic.setGotoScene(getGotoScene());
        arithmetic.setMathematicalExpression(getMathematicalExpression());

        return arithmetic;
    }

    public GotoScene getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }

    public String getMathematicalExpression() {
        return mathematicalExpression;
    }
    public void setMathematicalExpression(String mathematicalExpression) {
        this.mathematicalExpression = mathematicalExpression;
    }
}
