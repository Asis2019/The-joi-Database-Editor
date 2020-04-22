package com.asis.joi.model;

import com.asis.joi.model.components.Scene;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class JOI implements JOISystemInterface {
    private int sceneIdCounter = 0;
    private ArrayList<Scene> sceneArrayList = new ArrayList<>();

    public void addNewScene(Integer... optional) {
        //Check if an id was passed
        int sceneId = optional.length > 0 ? optional[0] : getSceneIdCounter();

        //Change the counter to the highest id
        if(sceneId > getSceneIdCounter()) {
            setSceneIdCounter(sceneId);
        }

        //Add new scene
        getSceneArrayList().add(new Scene(sceneId));

        //Increment counter
        setSceneIdCounter(getSceneIdCounter()+1);
    }
    public boolean removeScene(int sceneId) {
        return getSceneArrayList().remove(getScene(sceneId));
    }

    public JSONObject getJOIAsJson() {
        //Set values
        JSONArray joiArray = new JSONArray();
        for(Scene scene: getSceneArrayList()) {
            joiArray.put(scene.getSceneAsJson());
        }

        JSONObject finalWrapper = new JSONObject();
        return finalWrapper.put("JOI", joiArray);
    }

    public Scene getScene(int sceneId) {
        for(Scene scene: getSceneArrayList()) {
            if (scene.getSceneId() == sceneId) {
                return scene;
            }
        }
        return null;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        JSONArray array = jsonObject.getJSONArray("JOI");
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).has("sceneId")) {
                addNewScene(array.getJSONObject(i).getInt("sceneId"));
                getSceneArrayList().get(i).setDataFromJson(array.getJSONObject(i), importDirectory);
            } else {
                throw new RuntimeException("Scene id was not present for one or more of the scenes.");
            }
        }
    }

    @Override
    public String toString() {
        return getJOIAsJson().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof JOI)) return false;

        JOI joi = (JOI) object;

        if (getSceneIdCounter() != joi.getSceneIdCounter()) return false;
        return getSceneArrayList().equals(joi.getSceneArrayList());
    }

    //Getters and Setters
    public ArrayList<Scene> getSceneArrayList() {
        return sceneArrayList;
    }
    public void setSceneArrayList(ArrayList<Scene> sceneArrayList) {
        this.sceneArrayList = sceneArrayList;
    }

    public int getSceneIdCounter() {
        return sceneIdCounter;
    }
    private void setSceneIdCounter(int sceneIdCounter) {
        this.sceneIdCounter = sceneIdCounter;
    }
}