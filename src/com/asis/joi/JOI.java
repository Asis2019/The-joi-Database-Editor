package com.asis.joi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JOI {
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
    public String toString() {
        return getJOIAsJson().toString(4);
    }

    //Getters and Setters
    private ArrayList<Scene> getSceneArrayList() {
        return sceneArrayList;
    }
    private void setSceneArrayList(ArrayList<Scene> sceneArrayList) {
        this.sceneArrayList = sceneArrayList;
    }

    private int getSceneIdCounter() {
        return sceneIdCounter;
    }
    private void setSceneIdCounter(int sceneIdCounter) {
        this.sceneIdCounter = sceneIdCounter;
    }
}
