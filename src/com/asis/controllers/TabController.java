package com.asis.controllers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.json.JSONObject;

abstract class TabController {

    void beatProperties(JSONObject textObject, CheckBox checkBox, String key) {
        if (textObject != null && textObject.has(key)) {
            checkBox.setSelected(true);
        } else {
            checkBox.setSelected(false);
        }
    }

    void changeBeat(JSONObject textObject, TextField textField, String key) {
        if (textObject != null && textObject.has(key)) {
            double speed = textObject.getDouble(key);
            textField.setText(String.valueOf(speed));
        } else {
            textField.clear();
        }
    }
}
