package com.asis.joi.model;

import com.asis.joi.model.entites.Scene;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.ArrayList;

public class JOI implements JSONString, JOIEntity<JSONObject>, Cloneable {
    private int sceneIdCounter = 0;
    private ArrayList<Scene> sceneArrayList = new ArrayList<>();

    public void addNewScene(Integer... optional) {
        //Check if an id was passed
        int sceneId = optional.length > 0 ? optional[0] : getSceneIdCounter();

        //Change the counter to the highest id
        if(sceneId > getSceneIdCounter()) setSceneIdCounter(sceneId);

        //Add new scene
        getSceneArrayList().add(new Scene(sceneId));

        //Increment counter
        setSceneIdCounter(getSceneIdCounter()+1);

        System.out.println(getSceneIdCounter());
    }

    public boolean removeScene(int sceneId) {
        return getSceneArrayList().remove(getScene(sceneId));
    }

    public Scene getScene(int sceneId) {
        for(Scene scene: getSceneArrayList()) if (scene.getSceneId() == sceneId) return scene;
        return null;
    }

    public static JOI createEntity(JSONObject jsonObject) {
        JOI joi = new JOI();

        JSONArray array = jsonObject.getJSONArray("JOI");
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).has("sceneId")) {
                Scene scene = Scene.createEntity(array.getJSONObject(i));
                joi.getSceneArrayList().add(scene);
                if(array.getJSONObject(i).getInt("sceneId") >= joi.getSceneIdCounter()) {
                    joi.setSceneIdCounter(array.getJSONObject(i).getInt("sceneId")+1);
                }
            } else {
                throw new RuntimeException("Scene id was not present for one or more of the scenes.");
            }
        }

        return joi;
    }

    public double getDuration() {
        return getSceneArrayList().stream().mapToDouble(Scene::getDuration).sum();
    }

    @Override
    public JSONObject toJSON() {
        JSONArray joiArray = new JSONArray();
        for(Scene scene: getSceneArrayList()) joiArray.put(scene.toJSON());

        JSONObject finalWrapper = new JSONObject();
        return finalWrapper.put("JOI", joiArray);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public JOI clone() throws CloneNotSupportedException {
        JOI joi = (JOI) super.clone();

        ArrayList<Scene> clonedArray = new ArrayList<>();
        for (Scene scene: getSceneArrayList()) clonedArray.add(scene.clone());
        joi.setSceneArrayList(clonedArray);
        joi.setSceneIdCounter(getSceneIdCounter());

        return joi;
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
    private void setSceneArrayList(ArrayList<Scene> sceneArrayList) {
        this.sceneArrayList = sceneArrayList;
    }

    public int getSceneIdCounter() {
        return sceneIdCounter;
    }
    private void setSceneIdCounter(int sceneIdCounter) {
        this.sceneIdCounter = sceneIdCounter;
    }

}
