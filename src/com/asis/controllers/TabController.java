package com.asis.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONObject;

public abstract class TabController {

    // for TabNormalOperationController
    @FXML protected TextArea textTextField, mainTextArea;
    @FXML protected TextField textFieldBeatPitch, textFieldBeatSpeed;
    @FXML protected ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML protected VBox iconControllerBox;
    @FXML protected StackPane stackPane;
    @FXML protected Label lineCounterLabel;
    @FXML protected Button deleteLineButton, previousLineButton;
    @FXML protected CheckBox checkBoxStopBeat, checkBoxStartBeat;

    // for TabTimerController
    @FXML protected TextField goToSecondsTextField, totalTimerField;
    @FXML protected VBox timerIconControllerBox;
    @FXML protected StackPane timerStackPane;
    @FXML protected TextArea timerTextArea, textTextArea;
    @FXML protected HBox container;
    @FXML protected Label warningLabel;
    @FXML protected TreeView<String> objectTree;

    void updateComponents(JSONObject textObject) {

        if(textObject != null) {
            if(textObject.has("fillColor")) {
                textColorPicker.setValue(Color.web(textObject.getString("fillColor")));
            }

            if(textObject.has("outlineColor")) {
                textOutlineColorPicker.setValue(Color.web(textObject.getString("outlineColor")));
            }

            if(textObject.has("text")) {
                String text = textObject.getString("text").replaceAll("#", "\n");
                mainTextArea.setText(text);
            }

            if(textObject.has("startBeat")) {
                checkBoxStartBeat.setSelected(true);
            } else {
                checkBoxStartBeat.setSelected(false);
            }

            if(textObject.has("stopBeat")) {
                checkBoxStopBeat.setSelected(true);
            } else {
                checkBoxStopBeat.setSelected(false);
            }

            if(textObject.has("changeBeatSpeed")) {
                int speed = textObject.getInt("changeBeatSpeed");
                textFieldBeatSpeed.setText(String.valueOf(speed));
            } else {
                textFieldBeatSpeed.clear();
            }

            if(textObject.has("changeBeatPitch")) {
                double speed = textObject.getDouble("changeBeatPitch");
                textFieldBeatPitch.setText(String.valueOf(speed));
            } else {
                textFieldBeatPitch.clear();
            }
        }
    }

}
