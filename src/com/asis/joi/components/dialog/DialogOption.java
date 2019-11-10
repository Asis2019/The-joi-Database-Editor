package com.asis.joi.components.dialog;

import com.asis.joi.JOISystemInterface;
import com.asis.joi.components.GotoScene;
import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class DialogOption implements JOISystemInterface {
    private int optionNumber = 0;
    private String optionText = "";
    private GotoScene gotoScene;

    public DialogOption(int optionNumber, String optionText) {
        setOptionNumber(optionNumber);
        setOptionText(optionText);
    }

    public JSONObject getDialogOptionAsJson() {
        JSONObject object = new JSONObject();
        object.put("text", getOptionText());
        if(getGotoScene() != null)  AsisUtils.mergeJSONObjects(object, getGotoScene().getGotoSceneAsJson());

        JSONArray wrapper = new JSONArray();
        wrapper.put(object);

        JSONObject finalObject = new JSONObject();
        finalObject.put("option"+getOptionNumber(), wrapper);
        return finalObject;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        //Set option text
        if(jsonObject.has("text")) {
            setOptionText(jsonObject.getString("text"));
        }

        //set GotoScene
        if (jsonObject.has("gotoScene")) {
            setGotoScene(new GotoScene());
            getGotoScene().addValue(jsonObject.getInt("gotoScene"));
        }

        //set GotoSceneInRange
        if (jsonObject.has("gotoSceneInRange")) {
            setGotoScene(new GotoScene());
            getGotoScene().setDataFromJson(jsonObject.getJSONArray("gotoSceneInRange"));
        }
    }

    @Override
    public String toString() {
        return getDialogOptionAsJson().toString(4);
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
