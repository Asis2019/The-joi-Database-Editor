package com.asis.controllers.tabs;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.util.Optional;

public abstract class TabController {
    private boolean isClosable = true;
    private String tabTitle = "";

    TabController(String tabTitle) {
        setTabTitle(tabTitle);
    }

    void setMainTextAreaColorStyle(TextArea textArea, String fillColor, String outlineColor) {
        textArea.setStyle(String.format("outline-color: %s;fill-color: %s;", outlineColor,fillColor));
    }

    static void beatProperties(JSONObject textObject, CheckBox checkBox, String key) {
        if (textObject != null && textObject.has(key)) {
            checkBox.setSelected(true);
        } else {
            checkBox.setSelected(false);
        }
    }
    static void changeBeat(JSONObject textObject, TextField textField, String key) {
        if (textObject != null && textObject.has(key)) {
            double speed = textObject.getDouble(key);
            textField.setText(String.valueOf(speed));
        } else {
            textField.clear();
        }
    }
    static String removeLastTwoLetters(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 2))
                .orElse(s);
    }

    //Getters and setters
    public String getTabTitle() {
        return tabTitle;
    }
    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public boolean isClosable() {
        return isClosable;
    }
    public void setClosable(boolean closable) {
        isClosable = closable;
    }
}
