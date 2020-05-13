package com.asis.joi.model.entites.dialog;

import com.asis.joi.model.entites.GotoScene;
import com.asis.joi.model.entites.SceneComponent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

public class DialogOption implements JSONString, SceneComponent<JSONArray> {
    private int optionNumber = 0;
    private String optionText = "";
    private GotoScene gotoScene;

    public static DialogOption createEntity(JSONObject jsonObject) {
        DialogOption dialogOption = new DialogOption();

        //Set option number
        dialogOption.setOptionNumber(jsonObject.getInt("id"));

        //Set option text
        if(jsonObject.has("text")) dialogOption.setOptionText(jsonObject.getString("text"));

        //set GotoScene
        if (jsonObject.has("gotoScene")) {
            JSONObject gotoObject = new JSONObject();
            gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt("gotoScene")}));

            dialogOption.setGotoScene(GotoScene.createEntity(gotoObject));
        }

        //set GotoSceneInRange
        if (jsonObject.has("gotoSceneInRange")) {
            JSONObject gotoObject = new JSONObject();
            gotoObject.put("array", jsonObject.getJSONArray("gotoSceneInRange"));

            dialogOption.setGotoScene(GotoScene.createEntity(gotoObject));
        }

        return dialogOption;
    }

    public double getDuration() {
        return getOptionText().length() / 15d;
    }

    @Override
    public String jsonKeyName() {
        return "option"+getOptionNumber();
    }

    @Override
    public JSONArray toJSON() {
        JSONObject object = new JSONObject();
        object.put("text", getOptionText());
        if(getGotoScene() != null)  object.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        JSONArray wrapper = new JSONArray();
        return wrapper.put(object);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public DialogOption clone() throws CloneNotSupportedException {
        DialogOption dialogOption = (DialogOption) super.clone();

        dialogOption.setGotoScene(getGotoScene()==null?null:getGotoScene().clone());
        dialogOption.setOptionNumber(getOptionNumber());
        dialogOption.setOptionText(getOptionText());

        return dialogOption;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof DialogOption)) return false;

        DialogOption that = (DialogOption) object;

        if (getOptionNumber() != that.getOptionNumber()) return false;
        if (!getOptionText().equals(that.getOptionText())) return false;
        return getGotoScene() != null ? getGotoScene().equals(that.getGotoScene()) : that.getGotoScene() == null;
    }

    //Getters and Setters
    public String getOptionText() {
        return optionText;
    }
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public int getOptionNumber() {
        return optionNumber;
    }
    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }

    public GotoScene getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }
}
