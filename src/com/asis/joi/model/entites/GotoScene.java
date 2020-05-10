package com.asis.joi.model.entites;

import com.asis.utilities.AsisUtils;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.ArrayList;

public class GotoScene implements JSONString, JOIEntity<JSONObject>, Cloneable {
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
        if(getGotoSceneArrayList().size() > 1) setJsonKeyName("gotoSceneInRange");
        else setJsonKeyName("gotoScene");
    }

    public Object getJsonValue() {
        if(getJsonKeyName().equals("gotoScene")) return getGotoSceneArrayList().get(0);
        else return getGotoSceneArrayList().toArray();
    }

    public static GotoScene createEntity(JSONObject jsonObject) {
        GotoScene gotoScene = new GotoScene();

        if(jsonObject.has("array")) {
            for (Integer number : AsisUtils.convertJSONArrayToList(jsonObject.getJSONArray("array"))) {
                gotoScene.addValue(number);
            }
        }

        return gotoScene;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put(getJsonKeyName(), getJsonValue());
        return object;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public GotoScene clone() throws CloneNotSupportedException {
        GotoScene gotoScene = (GotoScene) super.clone();

        gotoScene.setJsonKeyName(getJsonKeyName());
        gotoScene.setGotoSceneArrayList(new ArrayList<>(getGotoSceneArrayList()));

        return gotoScene;
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
    private void setGotoSceneArrayList(ArrayList<Integer> gotoSceneArrayList) {
        this.gotoSceneArrayList = gotoSceneArrayList;
    }

    public String getJsonKeyName() {
        return jsonKeyName;
    }
    public void setJsonKeyName(String jsonKeyName) {
        this.jsonKeyName = jsonKeyName;
    }
}
