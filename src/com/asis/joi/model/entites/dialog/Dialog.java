package com.asis.joi.model.entites.dialog;

import com.asis.joi.model.entites.JOIEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.ArrayList;

public class Dialog implements JSONString, JOIEntity<JSONArray>, Cloneable {
    private ArrayList<DialogOption> optionArrayList = new ArrayList<>();

    public void addDialogOption() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getOptionArrayList().size());
        getOptionArrayList().add(DialogOption.createEntity(jsonObject));
    }

    public static Dialog createEntity(JSONObject jsonObject) {
        Dialog dialog = new Dialog();

        int i=0;
        while(jsonObject.has("option"+i)) {
            JSONObject optionObject = jsonObject.getJSONArray("option"+i).getJSONObject(0);
            optionObject.put("id", i);

            dialog.getOptionArrayList().add(DialogOption.createEntity(optionObject));
            i++;
        }

        return dialog;
    }

    @Override
    public JSONArray toJSON() {
        JSONObject object = new JSONObject();

        for(DialogOption dialogOption: getOptionArrayList())
            object.put("option" + dialogOption.getOptionNumber(), dialogOption.toJSON());

        JSONArray arrayOfOptions = new JSONArray();
        return arrayOfOptions.put(object);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public Dialog clone() throws CloneNotSupportedException {
        Dialog dialog = (Dialog) super.clone();

        ArrayList<DialogOption> clonedArray = new ArrayList<>();
        for (DialogOption dialogOption: getOptionArrayList()) clonedArray.add(dialogOption.clone());
        dialog.setOptionArrayList(clonedArray);

        return dialog;
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
    private void setOptionArrayList(ArrayList<DialogOption> optionArrayList) {
        this.optionArrayList = optionArrayList;
    }
}
