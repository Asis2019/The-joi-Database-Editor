package com.asis.joi.model.components.dialog;

import com.asis.joi.model.JOISystemInterface;
import com.asis.joi.model.components.FirstLevelEffect;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Dialog implements JOISystemInterface, FirstLevelEffect {
    private ArrayList<DialogOption> optionArrayList = new ArrayList<>();

    public void addDialogOption() {
        getOptionArrayList().add(new DialogOption(getOptionArrayList().size(), ""));
    }
    public void removeDialogOption(int dialogOptionsPosition) {
        getOptionArrayList().remove(dialogOptionsPosition);
    }

    public JSONArray getDialogAsJson() {
        JSONObject object = new JSONObject();

        for(DialogOption dialogOption: getOptionArrayList()) {
            object.put("option"+dialogOption.getOptionNumber(), dialogOption.getDialogOptionAsJson());
        }

        JSONArray arrayOfOptions = new JSONArray();
        return arrayOfOptions.put(object);
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        //set dialog options
        int i=0;
        while(jsonObject.has("option"+i)) {
            addDialogOption();
            getOptionArrayList().get(i).setDataFromJson(jsonObject.getJSONArray("option"+i).getJSONObject(0), importDirectory);
            i++;
        }
    }

    @Override
    public String toString() {
        return getDialogAsJson().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Dialog)) return false;

        Dialog dialog = (Dialog) object;

        return getOptionArrayList().equals(dialog.getOptionArrayList());
    }

    //Getters and Setters
    public ArrayList<DialogOption> getOptionArrayList() {
        return optionArrayList;
    }
    public void setOptionArrayList(ArrayList<DialogOption> optionArrayList) {
        this.optionArrayList = optionArrayList;
    }
}
