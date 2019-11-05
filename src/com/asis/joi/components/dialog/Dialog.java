package com.asis.joi.components.dialog;

import org.json.JSONArray;

import java.util.ArrayList;

public class Dialog {
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
