package com.asis.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public abstract class TabController {

    @FXML protected TextArea textTextField, mainTextArea;
    @FXML protected TextField textFieldBeatPitch, textFieldBeatSpeed;
    @FXML protected ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML protected VBox iconControllerBox;
    @FXML protected StackPane stackPane;
    @FXML protected Label lineCounterLabel;
    @FXML protected Button deleteLineButton, previousLineButton;
    @FXML protected CheckBox checkBoxStopBeat, checkBoxStartBeat;

}
