package com.asis.joi.model;

import com.asis.joi.JOIPackageManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MetaData implements JOISystemInterface, Cloneable {
    private File joiIcon;
    private String preparations, name, joiId, versionAdded, displayedFetishes, creator;
    private ArrayList<String> fetishList = new ArrayList<>();
    private ArrayList<String> characterList = new ArrayList<>();
    private ArrayList<String> equipmentList = new ArrayList<>();

    public JSONObject getMetaDataAsJson() {
        JSONObject innerObject = new JSONObject();
        addStringToJsonWithDefault(innerObject, getName(), getJoiId(),"joiId");
        addStringToJsonWithDefault(innerObject, "No preparations needed.", getPreparations(),"preparations");
        addStringToJsonWithDefault(innerObject, "", getName(),"name");
        addStringToJsonWithDefault(innerObject, "", getVersionAdded(),"versionAdded");
        addStringToJsonWithDefault(innerObject, "", getDisplayedFetishes(),"displayedFetishes");
        addStringToJsonWithDefault(innerObject, "", getCreator(),"creator");

        addListToJsonObject(innerObject, "fetish", getFetishList());
        addListToJsonObject(innerObject, "character", getCharacterList());
        addListToJsonObject(innerObject, "toy", getEquipmentList());

        JSONArray array = new JSONArray();
        array.put(innerObject);
        JSONObject finalObject = new JSONObject();
        return finalObject.put("JOI METADATA", array);
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        //Set single normal fields
        setData(jsonObject.keys(), jsonObject);

        //set fetishList
        populateListFromCategory(jsonObject, getFetishList(), "fetish");

        //set characterList
        populateListFromCategory(jsonObject, getCharacterList(), "character");

        //set equipmentList
        populateListFromCategory(jsonObject, getEquipmentList(), "toy");
    }

    private void setData(Iterator<String> keys, JSONObject object) {
        while (keys.hasNext()) {
            setValueAccordingToKey(object, keys.next());
        }
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
    public MetaData clone() throws CloneNotSupportedException {
        MetaData metaData = (MetaData) super.clone();

        JSONObject object = getMetaDataAsJson().getJSONArray("JOI METADATA").getJSONObject(0);
        metaData.setDataFromJson(object, JOIPackageManager.getInstance().getJoiPackageDirectory());

        return metaData;
    }

    @Override
    public String toString() {
        return getMetaDataAsJson().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MetaData)) return false;

        MetaData metaData = (MetaData) object;

        if (getJoiIcon() != null ? !getJoiIcon().equals(metaData.getJoiIcon()) : metaData.getJoiIcon() != null)
            return false;

        if (getPreparations() != null ? !getPreparations().equals(metaData.getPreparations()) : metaData.getPreparations() != null)
            return false;

        if (getName() != null ? !getName().equals(metaData.getName()) : metaData.getName() != null) return false;

        if (getJoiId() != null ? !getJoiId().equals(metaData.getJoiId()) : metaData.getJoiId() != null) return false;

        if (getVersionAdded() != null ? !getVersionAdded().equals(metaData.getVersionAdded()) : metaData.getVersionAdded() != null)
            return false;

        if (getDisplayedFetishes() != null ? !getDisplayedFetishes().equals(metaData.getDisplayedFetishes()) : metaData.getDisplayedFetishes() != null)
            return false;

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
        for (int i = 0; i < list.size(); i++) {
            object.put(key + i, list.get(i));
        }
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

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
}
