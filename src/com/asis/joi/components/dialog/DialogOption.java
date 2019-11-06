package com.asis.joi.components.dialog;

import com.asis.joi.JOISystemInterface;
import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class DialogOption implements JOISystemInterface {
    private int optionNumber = 0;
    private String optionText = "";
    private ArrayList<Integer> gotoSceneArrayList = new ArrayList<>();

    public DialogOption(int optionNumber, String optionText) {
        setOptionNumber(optionNumber);
        setOptionText(optionText);
    }

    public JSONObject getDialogOptionAsJson() {
        JSONObject object = new JSONObject();
        object.put("text", getOptionText());
        if(!getGotoSceneArrayList().isEmpty()) object.put("gotoScene", getGotoSceneArrayList().toArray());

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

        //set gotoScene
        if (jsonObject.has("gotoScene")) {
            setGotoSceneArrayList(AsisUtils.convertJSONArrayToList(jsonObject.getJSONArray("gotoScene")));
        }
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

    public int getOptionNumber() {
        return optionNumber;
    }
    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }

    public ArrayList<Integer> getGotoSceneArrayList() {
        return gotoSceneArrayList;
    }
    public void setGotoSceneArrayList(ArrayList<Integer> gotoSceneArrayList) {
        this.gotoSceneArrayList = gotoSceneArrayList;
    }
}
