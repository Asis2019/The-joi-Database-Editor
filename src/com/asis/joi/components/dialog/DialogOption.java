package com.asis.joi.components.dialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class DialogOption {
    private int gotoScene, optionNumber;
    private String optionText;

    public DialogOption() {
        this(0, "");
    }
    public DialogOption(int optionNumber, String optionText) {
        setOptionNumber(optionNumber);
        setOptionText(optionText);
        setGotoScene(-1);
    }

    public JSONObject getDialogOptionAsJson() {
        JSONObject object = new JSONObject();
        object.put("text", getOptionText());
        if(getGotoScene() != -1) object.put("gotoScene", getGotoScene());

        JSONArray wrapper = new JSONArray();
        wrapper.put(object);

        JSONObject finalObject = new JSONObject();
        finalObject.put("option"+getOptionNumber(), wrapper);
        return finalObject;
    }

    @Override
    public String toString() {
        return getDialogOptionAsJson().toString(4);
    }

    //Getters and Setters
    public String getOptionText() {
        return optionText;
    }
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public int getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(int gotoScene) {
        this.gotoScene = gotoScene;
    }

    public int getOptionNumber() {
        return optionNumber;
    }
    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }
}
