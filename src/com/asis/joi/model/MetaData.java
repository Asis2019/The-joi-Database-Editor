package com.asis.joi.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MetaData implements Cloneable, JOIEntity<JSONObject>, JSONString {
    private File joiIcon;
    private String preparations="", name="", joiId="", versionAdded="", displayedFetishes="", creator="";
    private double estimatedDuration = 0;
    private ArrayList<String> fetishList = new ArrayList<>();
    private ArrayList<String> characterList = new ArrayList<>();
    private ArrayList<String> equipmentList = new ArrayList<>();

    public void setDataFromJson(JSONObject jsonObject) {
        //Set single normal fields
        for (String key: jsonObject.keySet()) setValueAccordingToKey(jsonObject, key);

        //set fetishList
        populateListFromCategory(jsonObject, getFetishList(), "fetish");

        //set characterList
        populateListFromCategory(jsonObject, getCharacterList(), "character");

        //set equipmentList
        populateListFromCategory(jsonObject, getEquipmentList(), "toy");
    }

    private void setValueAccordingToKey(JSONObject jsonObject, String key) {
        switch (key) {
            case "preparations":
                setPreparations(jsonObject.getString("preparations"));
                break;
            case "name":
                setName(jsonObject.getString("name"));
                break;
            case "creator":
                setCreator(jsonObject.getString("creator"));
                break;
            case "joiId":
                setJoiId(jsonObject.getString("joiId"));
                break;
            case "versionAdded":
                setVersionAdded(jsonObject.getString("versionAdded"));
                break;
            case "displayedFetishes":
                setDisplayedFetishes(jsonObject.getString("displayedFetishes"));
                break;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject innerObject = new JSONObject();
        addStringToJsonWithDefault(innerObject, getName(), getJoiId(),"joiId");
        addStringToJsonWithDefault(innerObject, "", getPreparations(),"preparations");
        addStringToJsonWithDefault(innerObject, "", getName(),"name");
        addStringToJsonWithDefault(innerObject, "", getVersionAdded(),"versionAdded");
        addStringToJsonWithDefault(innerObject, "", getDisplayedFetishes(),"displayedFetishes");
        addStringToJsonWithDefault(innerObject, "", getCreator(),"creator");

        innerObject.put("estimatedDuration", getEstimatedDuration());

        addListToJsonObject(innerObject, "fetish", getFetishList());
        addListToJsonObject(innerObject, "character", getCharacterList());
        addListToJsonObject(innerObject, "toy", getEquipmentList());

        JSONArray array = new JSONArray();
        array.put(innerObject);
        return new JSONObject().put("JOI METADATA", array);
    }

    @Override
    public MetaData clone() throws CloneNotSupportedException {
        MetaData metaData = (MetaData) super.clone();

        metaData.setJoiIcon(getJoiIcon());
        metaData.setPreparations(getPreparations());
        metaData.setName(getName());
        metaData.setJoiId(getJoiId());
        metaData.setVersionAdded(getVersionAdded());
        metaData.setDisplayedFetishes(getDisplayedFetishes());
        metaData.setCreator(getCreator());

        metaData.setFetishList(new ArrayList<>(getFetishList()));
        metaData.setCharacterList(new ArrayList<>(getCharacterList()));
        metaData.setEquipmentList(new ArrayList<>(getEquipmentList()));

        return metaData;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaData)) return false;

        MetaData metaData = (MetaData) o;

        if (!getPreparations().equals(metaData.getPreparations())) return false;
        if (!getName().equals(metaData.getName())) return false;
        if (!getJoiId().equals(metaData.getJoiId())) return false;
        if (!getVersionAdded().equals(metaData.getVersionAdded())) return false;
        if (!getDisplayedFetishes().equals(metaData.getDisplayedFetishes())) return false;
        if (!getCreator().equals(metaData.getCreator())) return false;
        if (!getFetishList().equals(metaData.getFetishList())) return false;
        if (!getCharacterList().equals(metaData.getCharacterList())) return false;
        return getEquipmentList().equals(metaData.getEquipmentList());
    }

    public static void addCommaSeparatedStringToList(String commaSeparatedString, ArrayList<String> list) {
        if(commaSeparatedString != null && !commaSeparatedString.trim().isEmpty()) {
            list.clear();
            list.addAll(Arrays.asList(commaSeparatedString.trim().split("\\s*,\\s*")));
        }
    }

    private static <T> void addListToJsonObject(JSONObject object, String key, ArrayList<T> list) {
        for (int i = 0; i < list.size(); i++) object.put(key + i, list.get(i));
    }

    private static void populateListFromCategory(JSONObject jsonObject, ArrayList<String> listToPopulate, String categoryKey) {
        int i=0;
        while(jsonObject.has(categoryKey+i)) {
            listToPopulate.add(jsonObject.getString(categoryKey+i));
            i++;
        }
    }

    private static void addStringToJsonWithDefault(JSONObject object, String defaultValue, String value, String key) {
        object.put(key, value != null && !value.isEmpty() ? value : defaultValue);
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
    private void setFetishList(ArrayList<String> fetishList) {
        this.fetishList = fetishList;
    }

    public ArrayList<String> getCharacterList() {
        return characterList;
    }
    private void setCharacterList(ArrayList<String> characterList) {
        this.characterList = characterList;
    }

    public ArrayList<String> getEquipmentList() {
        return equipmentList;
    }
    private void setEquipmentList(ArrayList<String> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public File getJoiIcon() {
        return joiIcon;
    }
    public void setJoiIcon(File joiIcon) {
        this.joiIcon = joiIcon;
    }

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }

    public double getEstimatedDuration() {
        return estimatedDuration;
    }
    public void setEstimatedDuration(double estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
}
