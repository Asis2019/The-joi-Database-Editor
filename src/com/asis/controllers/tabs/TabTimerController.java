package com.asis.controllers.tabs;

import com.asis.controllers.AddableSceneImage;
import com.asis.joi.model.entities.Line;
import com.asis.joi.model.entities.SceneImage;
import com.asis.joi.model.entities.Timer;
import com.asis.ui.AsisCenteredArc;
import com.asis.ui.ImageViewPane;
import com.asis.ui.NumberField;
import com.asis.utilities.AsisUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Comparator;
import java.util.NoSuchElementException;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabTimerController extends TabController implements AddableSceneImage {
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalSeconds = 0;
    private int onSecond = 0;
    private boolean lockTextAreaFunctionality = false;

    private final ImageViewPane viewPane = new ImageViewPane();
    private final AsisCenteredArc asisCenteredArc = new AsisCenteredArc();

    private Timer timer;

    @FXML
    private NumberField goToSecondsTextField, totalTimerField, textFieldBeatPitch, textFieldBeatSpeed, imageSpeedMultiplier;
    @FXML
    private ColorPicker textColorPicker, textOutlineColorPicker, timerTextColorPicker, timerTextOutlineColorPicker;
    @FXML
    private VBox timerIconControllerBox, timerTextColorContainer;
    @FXML
    private StackPane timerStackPane;
    @FXML
    private TextArea timerTextArea, textTextArea;
    @FXML
    private HBox container;
    @FXML
    private CheckBox checkBoxStopBeat, checkBoxStartBeat, checkBoxHideTime, checkBoxHideTimer, checkStopAmbience;
    @FXML
    private Label warningLabel;
    @FXML
    private TreeView<String> objectTree;
    @FXML
    private Button deleteLineButton;

    public TabTimerController(String tabTitle, Timer timer) {
        super(tabTitle);

        setTimer(timer);

        Platform.runLater(() -> {
            setNodeColorStyle(textTextArea, fillColor, outlineColor);

            //Setup text area
            textTextArea.textProperty().bindBidirectional(timerTextArea.textProperty());

            textTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!isLockTextAreaFunctionality()) {
                    if (getTimer().getLineGroup().getLine(onSecond) == null) {
                        getTimer().getLineGroup().addNewLine(onSecond);
                        deleteLineButton.setDisable(false);
                    }

                    final String formattedText = newValue.replaceAll("\\n", "#");

                    getTimer().getLineGroup().getLine(onSecond).setText(formattedText);
                    getTimer().getLineGroup().getLine(onSecond).setFillColor(fillColor);
                    getTimer().getLineGroup().getLine(onSecond).setOutlineColor(outlineColor);

                    updateObjectTree();
                }
            });

            addColorPickerListeners();

            //Setup total time field
            totalTimerField.textProperty().addListener((observable, s, t1) -> {
                totalSeconds = totalTimerField.getIntegerNumber(0);
                asisCenteredArc.setMaxLength(totalSeconds);

                handleSecondsOverTotal();
                getTimer().setTotalTime(totalSeconds);
                updateObjectTree();
            });

            //Setup go to seconds field
            goToSecondsTextField.textProperty().addListener((observable, s, t1) -> {
                onSecond = goToSecondsTextField.getIntegerNumber(0);
                asisCenteredArc.setArcProgress(onSecond);

                if (!s.trim().isEmpty()) {
                    Line line = getTimer().getLineGroup().getLine(Integer.parseInt(s));
                    if (line != null) {
                        Line emptyLine = new Line();
                        emptyLine.setLineNumber(line.getLineNumber());

                        if (line.equals(emptyLine))
                            getTimer().getLineGroup().getLineArrayList().remove(getTimer().getLineGroup().getLine(Integer.parseInt(s)));
                    }
                }

                if (!isLockTextAreaFunctionality() && getTimer().getLineGroup().getLine(onSecond) == null)
                    getTimer().getLineGroup().addNewLine(onSecond);

                deleteLineButton.setDisable(getTimer().getLineGroup().getLineArrayList().size() == 0);

                handleSecondsOverTotal();
                setTextAreaVariables();
                setVisibleImage();

                updateObjectTree();
            });

            addCheckBoxFieldListeners();
            addBeatFieldListeners();

            //timer
            asisCenteredArc.setMaxLength(0);
            asisCenteredArc.setArcProgress(0);
            container.getChildren().add(asisCenteredArc.getArcPane());

            setTextAreaVariables();
            setVisibleImage();

            //total timer
            totalTimerField.setText(String.valueOf(getTimer().getTotalTime()));
            totalSeconds = Integer.parseInt(totalTimerField.getText().trim());
            asisCenteredArc.setMaxLength(totalSeconds);

            //Check boxes
            if (getTimer().isTimerHidden()) checkBoxHideTimer.setSelected(true);
            if (getTimer().isTimeHidden()) checkBoxHideTime.setSelected(true);

            //Update Tree View
            updateObjectTree();
        });
    }

    private void addColorPickerListeners() {
        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(timerTextArea, fillColor, outlineColor);
            if (getTimer().getLineGroup().getLine(onSecond) != null)
                getTimer().getLineGroup().getLine(onSecond).setOutlineColor(outlineColor);
            updateObjectTree();
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(timerTextArea, fillColor, outlineColor);
            if (getTimer().getLineGroup().getLine(onSecond) != null)
                getTimer().getLineGroup().getLine(onSecond).setFillColor(fillColor);
            updateObjectTree();
        });

        //Timer text color
        timerTextColorPicker.valueProperty().addListener((observableValue, color, t1) -> setTimerTextColors());
        timerTextOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> setTimerTextColors());
    }

    private void setTimerTextColors(String... optionalColorOverride) {
        if (optionalColorOverride.length == 0) {
            getTimer().setTimerTextColor(removeLastTwoLetters("#" + colorToHex(timerTextColorPicker.getValue())));
            getTimer().setTimerTextOutlineColor(removeLastTwoLetters("#" + colorToHex(timerTextOutlineColorPicker.getValue())));
            asisCenteredArc.setLabelColor(getTimer().getTimerTextColor(), getTimer().getTimerTextOutlineColor(), 1);
            updateObjectTree();
        } else if (optionalColorOverride.length == 2) {
            asisCenteredArc.setLabelColor(optionalColorOverride[0], optionalColorOverride[1], 0);
        }
    }

    private void addCheckBoxFieldListeners() {
        checkBoxHideTime.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            getTimer().setTimeHidden(newValue);
            asisCenteredArc.hideProgress(newValue);
            updateObjectTree();
        });

        checkBoxHideTimer.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            getTimer().setTimerHidden(newValue);
            asisCenteredArc.getArcPane().setVisible(!newValue);
            updateObjectTree();
        });
    }

    private void addBeatFieldListeners() {
        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            getTimer().getLineGroup().getLine(onSecond).setChangeBeatPitch(textFieldBeatPitch.getDoubleNumber());
            updateObjectTree();
        });

        //Setup beat fields
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat speed
            getTimer().getLineGroup().getLine(onSecond).setChangeBeatSpeed(textFieldBeatSpeed.getIntegerNumber());
            updateObjectTree();
        });
    }

    private void updateObjectTree() {
        TreeItem<String> root = new TreeItem<>("Timer");
        getObjectTree().setRoot(root);

        JSONObject timerObject = getTimer().toJSON().getJSONObject(0);

        if (timerObject != null && !timerObject.isEmpty()) {
            for (int i = 0; i < timerObject.names().length(); i++) {
                String key = timerObject.names().getString(i);
                Object object = timerObject.get(timerObject.names().getString(i));
                if (object instanceof JSONArray) {
                    TreeItem<String> item = new TreeItem<>(key);
                    root.getChildren().add(item);

                    JSONObject lineObject = ((JSONArray) object).getJSONObject(0);

                    for (int ii = 0; ii < lineObject.names().length(); ii++) {
                        String subKey = lineObject.names().getString(ii);
                        String subValue = lineObject.get(lineObject.names().getString(ii)).toString();

                        TreeItem<String> subItem = new TreeItem<>(subKey + ": " + subValue);
                        item.getChildren().add(subItem);
                    }
                    continue;
                }

                final String value = timerObject.get(timerObject.names().getString(i)).toString();
                root.getChildren().add(new TreeItem<>(key + ": " + value));
            }
        }

        root.getChildren().sort(Comparator.comparing(t -> t.getValue().length()));
        root.setExpanded(true);
    }

    public void actionAddImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            if (getScene(getTimer()) != null) {
                boolean result = createNewSceneImage(file, getScene(getTimer()));
                if (result) setVisibleImage();
            }
        }
    }

    public void actionAddLineImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null && getTimer().getLineGroup().getLine(onSecond) != null) {
            //Add image to json object
            getTimer().getLineGroup().getLine(onSecond).setLineImage(file);

            setVisibleImage();
        }
    }

    public void actionDeleteLine() {
        getTimer().getLineGroup().getLineArrayList().remove(getTimer().getLineGroup().getLine(onSecond));

        Line line = getTimer().getLineGroup().getMaxLine();
        if (line != null)
            goToSecondsTextField.setText(String.valueOf(line.getLineNumber()));
        else {
            setLockTextAreaFunctionality(true);
            goToSecondsTextField.clear();
            textTextArea.clear();
            deleteLineButton.setDisable(true);
            updateObjectTree();
            setLockTextAreaFunctionality(false);
        }
    }

    private File getImageFile() {
        //Create and set working file to passed in var if not null
        File workingFile = new File("");

        //Scene image code
        if (getScene(getTimer()) != null && getScene(getTimer()).hasComponent(SceneImage.class) &&
                getScene(getTimer()).getComponent(SceneImage.class).getImage() != null) {
            workingFile = getScene(getTimer()).getComponent(SceneImage.class).getImage();
            timerStackPane.getChildren().remove(timerIconControllerBox);
        }

        //Line image code
        if (getTimer().getLineGroup().getLine(onSecond) != null && getTimer().getLineGroup().getLine(onSecond).getLineImage() != null) {
            workingFile = getTimer().getLineGroup().getLine(onSecond).getLineImage();
        }
        return workingFile;
    }

    private void setTextAreaVariables() {
        setLockTextAreaFunctionality(true);
        timerTextArea.setText("");

        if (getTimer().getLineGroup().getLine(onSecond) != null) {
            setFieldsIfNeeded();

            JSONObject textObject = getTimer().getLineGroup().getLine(onSecond).toJSON().getJSONObject(0);
            beatConfiguration(textObject);
        } else
            imageSpeedMultiplier.clear();


        setLockTextAreaFunctionality(false);
    }

    private void setFieldsIfNeeded() {
        Line workingLine = getTimer().getLineGroup().getLine(onSecond);
        if (workingLine.getFrameRateMultiplier() != null)
            imageSpeedMultiplier.setText(workingLine.getFrameRateMultiplier().toString());

        if (workingLine.getFillColor() != null)
            textColorPicker.setValue(Color.web(workingLine.getFillColor()));

        if (workingLine.getOutlineColor() != null)
            textOutlineColorPicker.setValue(Color.web(workingLine.getOutlineColor()));

        if (workingLine.getStopAmbience() != null)
            checkStopAmbience.setSelected(true);

        timerTextArea.setText(workingLine.getText().replaceAll("#", "\n"));
    }

    private void beatConfiguration(JSONObject textObject) {
        beatProperties(textObject, checkBoxStartBeat, "startBeat");
        beatProperties(textObject, checkBoxStopBeat, "stopBeat");

        changeBeatSpeed(textObject, textFieldBeatSpeed, "changeBeatSpeed");
        changeBeatSpeed(textObject, textFieldBeatPitch, "changeBeatPitch");
    }

    private void handleSecondsOverTotal() {
        if (onSecond > totalSeconds) {
            warningLabel.setVisible(true);
            asisCenteredArc.setArcStrokeColor(Color.RED);
        } else {
            warningLabel.setVisible(false);
            asisCenteredArc.setArcStrokeColor(Color.GREEN);
        }
    }

    public void actionStartBeat() {
        setLineStartCheckBoxState(getTimer().getLineGroup().getLine(onSecond), checkBoxStartBeat);
        updateObjectTree();
    }

    public void actionStopBeat() {
        setLineStopCheckBoxState(getTimer().getLineGroup().getLine(onSecond), checkBoxStopBeat);
        updateObjectTree();
    }

    public void actionStopAmbience() {
        if (checkStopAmbience.isSelected())
            getTimer().getLineGroup().getLine(onSecond).setStopAmbience(checkStopAmbience.isSelected());
        else
            getTimer().getLineGroup().getLine(onSecond).setStopAmbience(null);
    }

    public void actionToggleTimerTextColorPickers() {
        timerTextColorContainer.setDisable(!timerTextColorContainer.isDisabled());
        if (timerTextColorContainer.isDisabled()) {
            getTimer().setTimerTextColor(null);
            getTimer().setTimerTextOutlineColor(null);
            setTimerTextColors("#ffffff", "#ffffff");
        } else {
            setTimerTextColors();
        }
        updateObjectTree();
    }

    public void setVisibleImage() {
        try {
            if (getScene(getTimer()).getComponent(SceneImage.class).getFrameRate() != -1) {
                imageSpeedMultiplier.setVisible(true);
                imageSpeedMultiplier.setManaged(true);
                imageSpeedMultiplier.textProperty().addListener((observableValue, s, t1) -> {
                    if (getTimer().getLineGroup().getLine(onSecond) != null) {
                        getTimer().getLineGroup().getLine(onSecond).setFrameRateMultiplier(imageSpeedMultiplier.getDoubleNumber());
                        updateObjectTree();
                    }
                });
            }
        } catch (NoSuchElementException ignore) {}

        setVisibleImage(getEditorWindow(), timerStackPane, viewPane, getImageFile(), getScene(getTimer()));
    }

    //Getters and setters
    private boolean isLockTextAreaFunctionality() {
        return lockTextAreaFunctionality;
    }

    private void setLockTextAreaFunctionality(boolean lockTextAreaFunctionality) {
        this.lockTextAreaFunctionality = lockTextAreaFunctionality;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    private TreeView<String> getObjectTree() {
        return objectTree;
    }
}
