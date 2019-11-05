package com.asis.joi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MetaData implements JOISystemInterface{
    private File joiIcon;
    private String preparations, name, joiId, versionAdded, displayedFetishes;
    private ArrayList<String> fetishList = new ArrayList<>();
    private ArrayList<String> characterList = new ArrayList<>();
    private ArrayList<String> equipmentList = new ArrayList<>();

    public JSONObject getMetaDataAsJson() {
        JSONObject innerObject = new JSONObject();
        innerObject.put("joiId", getJoiId());
        innerObject.put("preparations", getPreparations());
        innerObject.put("name", getName());
        innerObject.put("versionAdded", getVersionAdded());
        innerObject.put("displayedFetishes", getDisplayedFetishes());

        addListToJsonObject(innerObject, "fetish", getFetishList());
        addListToJsonObject(innerObject, "character", getCharacterList());
        addListToJsonObject(innerObject, "toy", getEquipmentList());

        JSONArray array = new JSONArray();
        array.put(innerObject);
        JSONObject finalObject = new JSONObject();
        return finalObject.put("JOI METADATA", array);
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject) {
        //Set preparations
        if(jsonObject.has("preparations")) {
            setPreparations(jsonObject.getString("preparations"));
        }

        //Set name
        if(jsonObject.has("name")) {
            setName(jsonObject.getString("name"));
        }

        //Set joiId
        if(jsonObject.has("joiId")) {
            setJoiId(jsonObject.getString("joiId"));
        }

        //Set versionAdded
        if(jsonObject.has("versionAdded")) {
            setVersionAdded(jsonObject.getString("versionAdded"));
        }

        //Set displayedFetishes
        if(jsonObject.has("displayedFetishes")) {
            setDisplayedFetishes(jsonObject.getString("displayedFetishes"));
        }

        //set fetishList
        int i=0;
        while(jsonObject.has("fetish"+i)) {
            getFetishList().add(jsonObject.getString("fetish"+i));
            i++;
        }

        //set characterList
        i=0;
        while(jsonObject.has("character"+i)) {
            getCharacterList().add(jsonObject.getString("character"+i));
            i++;
        }

        //set equipmentList
        i=0;
        while(jsonObject.has("toy"+i)) {
            getEquipmentList().add(jsonObject.getString("toy"+i));
            i++;
        }
    }

    @Override
    public String toString() {
        return getMetaDataAsJson().toString(4);
    }

    public static void addCommaSeparatedStringToList(String commaSeparatedString, ArrayList<String> list) {
        list.addAll(Arrays.asList(commaSeparatedString.trim().split("\\s*,\\s*")));
    }

    private static void addListToJsonObject(JSONObject object, String key, ArrayList list) {
        for(int i=0; i<list.size(); i++) {
            object.put(key+i, list.get(i));
        }
    }

    //Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getJoiId() {
        return joiId;
    }
    public void setJoiId(String joiId) {
        this.joiId = joiId;
    }

    public String getVersionAdded() {
        return versionAdded;
    }
    public void setVersionAdded(String versionAdded) {
        this.versionAdded = versionAdded;
    }

    public String getDisplayedFetishes() {
        return displayedFetishes;
    }
    public void setDisplayedFetishes(String displayedFetishes) {
        this.displayedFetishes = displayedFetishes;
    }

    public String getPreparations() {
        return preparations;
    }
    public void setPreparations(String preparations) {
        this.preparations = preparations;
    }

    public ArrayList<String> getFetishList() {
        return fetishList;
    }
    public void setFetishList(ArrayList<String> fetishList) {
        this.fetishList = fetishList;
    }

    public ArrayList<String> getCharacterList() {
        return characterList;
    }
    public void setCharacterList(ArrayList<String> characterList) {
        this.characterList = characterList;
    }

    public ArrayList<String> getEquipmentList() {
        return equipmentList;
    }
    public void setEquipmentList(ArrayList<String> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public File getJoiIcon() {
        return joiIcon;
    }
    public void setJoiIcon(File joiIcon) {
        this.joiIcon = joiIcon;
    }
}
