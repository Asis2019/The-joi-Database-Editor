package com.asis.joi.model.components;

import com.asis.joi.model.JOISystemInterface;
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

    public Object getJsonValue() {
        if(getJsonKeyName().equals("gotoScene")) {
            return getGotoSceneArrayList().get(0);
        } else {
            return getGotoSceneArrayList().toArray();
        }
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
        JSONObject object = new JSONObject();
        object.put(getJsonKeyName(), getJsonValue());
        return object.toString(4);
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
