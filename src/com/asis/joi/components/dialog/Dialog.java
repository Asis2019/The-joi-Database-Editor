package com.asis.joi.components.dialog;

import com.asis.joi.JOISystemInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Dialog implements JOISystemInterface {
    private ArrayList<DialogOption> optionArrayList = new ArrayList<>();


    public void addDialogOption() {
        getOptionArrayList().add(new DialogOption(getOptionArrayList().size(), ""));
    }
    public void removeDialogOption(int dialogOptionsPosition) {
        getOptionArrayList().remove(dialogOptionsPosition);
    }

    public JSONArray getDialogAsJson() {
        JSONArray arrayOfOptions = new JSONArray();
        for(DialogOption dialogOption: getOptionArrayList()) {
            arrayOfOptions.put(dialogOption.getDialogOptionAsJson());
        }

        return arrayOfOptions;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject) {
        //set dialog options
        int i = 0;
        while (jsonObject.has("option"+i)) {
            addDialogOption();
            getOptionArrayList().get(i).setDataFromJson(jsonObject.getJSONArray("option"+i).getJSONObject(0));
        }
    }

    @Override
    public String toString() {
        return getDialogAsJson().toString(4);
    }

    //Getters and Setters
    public ArrayList<DialogOption> getOptionArrayList() {
        return optionArrayList;
    }
    public void setOptionArrayList(ArrayList<DialogOption> optionArrayList) {
        this.optionArrayList = optionArrayList;
    }
}
