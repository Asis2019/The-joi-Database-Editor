package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class GotoScene implements JOISystemInterface {
    private String jsonKeyName = "gotoScene";

    private ArrayList<Integer> gotoSceneArrayList = new ArrayList<>();

    public void addValue(int gotoId) {
        getGotoSceneArrayList().add(gotoId);

        updateJsonKeyName();
    }
    public void removeValue(Integer gotoId) {
        getGotoSceneArrayList().remove(gotoId);

        updateJsonKeyName();
    }

    private void updateJsonKeyName() {
        if(getGotoSceneArrayList().size() > 1) {
            setJsonKeyName("gotoSceneInRange");
        } else {
            setJsonKeyName("gotoScene");
        }
    }

    public JSONObject getGotoSceneAsJson() {
        JSONObject object = new JSONObject();

        if(getJsonKeyName().equals("gotoScene")) {
            object.put(getJsonKeyName(), getGotoSceneArrayList().get(0));
        } else {
            object.put(getJsonKeyName(), getGotoSceneArrayList().toArray());
        }

        return object;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
    }

    public void setDataFromJson(JSONArray jsonArray) {
        for(Integer number: AsisUtils.convertJSONArrayToList(jsonArray)) {
            addValue(number);
        }
    }

    @Override
    public String toString() {
        return getGotoSceneAsJson().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GotoScene)) return false;

        GotoScene gotoScene = (GotoScene) object;

        if (!getJsonKeyName().equals(gotoScene.getJsonKeyName())) return false;
        return getGotoSceneArrayList().equals(gotoScene.getGotoSceneArrayList());
    }

    //Getters and setters
    public ArrayList<Integer> getGotoSceneArrayList() {
        return gotoSceneArrayList;
    }
    public void setGotoSceneArrayList(ArrayList<Integer> gotoSceneArrayList) {
        this.gotoSceneArrayList = gotoSceneArrayList;
    }

    public String getJsonKeyName() {
        return jsonKeyName;
    }
    public void setJsonKeyName(String jsonKeyName) {
        this.jsonKeyName = jsonKeyName;
    }
}
