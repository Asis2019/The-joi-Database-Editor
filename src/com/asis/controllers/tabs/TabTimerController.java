package com.asis.controllers.tabs;

import com.asis.joi.model.components.Timer;
import com.asis.ui.AsisCenteredArc;
import com.asis.ui.ImageViewPane;
import com.asis.utilities.AsisUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Comparator;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabTimerController extends TabController {
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalSeconds = 0;
    private int onSecond = 0;
    private boolean lockTextAreaFunctionality = false;

    private ImageViewPane viewPane = new ImageViewPane();
    private AsisCenteredArc asisCenteredArc = new AsisCenteredArc();

    private Timer timer;

    @FXML private TextField goToSecondsTextField, totalTimerField, textFieldBeatPitch, textFieldBeatSpeed;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox timerIconControllerBox;
    @FXML private StackPane timerStackPane;
    @FXML private TextArea timerTextArea, textTextArea;
    @FXML private HBox container;
    @FXML private CheckBox checkBoxStopBeat, checkBoxStartBeat, checkBoxHideTime, checkBoxHideTimer;
    @FXML private Label warningLabel;
    @FXML private TreeView<String> objectTree;

    public TabTimerController(String tabTitle, Timer timer) {
        super(tabTitle);

        setTimer(timer);

        Platform.runLater(() -> {
            setNodeColorStyle(textTextArea, fillColor, outlineColor);

            textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
                outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
                setNodeColorStyle(textTextArea, fillColor, outlineColor);
                if(getTimer().getLine(onSecond) != null) getTimer().getLine(onSecond).setOutlineColor(outlineColor);
            });

            textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
                fillColor = removeLastTwoLetters("#"+colorToHex(t1));
                setNodeColorStyle(textTextArea, fillColor, outlineColor);
                if(getTimer().getLine(onSecond) != null) getTimer().getLine(onSecond).setFillColor(fillColor);
            });

            textTextArea.textProperty().bindBidirectional(timerTextArea.textProperty());

            //Setup text area
            textTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if(!isLockTextAreaFunctionality()) {
                    final String formattedText = newValue.replaceAll("\\n", "#");

                    if (!newValue.isEmpty()) {
                        if(getTimer().getLine(onSecond) == null) {
                            getTimer().addNewLine(onSecond);
                        }

                        getTimer().getLine(onSecond).setText(formattedText);
                        getTimer().getLine(onSecond).setFillColor(fillColor);
                        getTimer().getLine(onSecond).setOutlineColor(outlineColor);
                    } else {
                        getTimer().removeLine(onSecond);
                    }

                    updateObjectTree();
                }
            });

            //Setup total time field
            totalTimerField.textProperty().addListener((observable, s, t1) -> {
                try {
                    totalSeconds = Integer.parseInt(totalTimerField.getText().trim());
                    asisCenteredArc.setMaxLength(totalSeconds);

                    handelSecondsOverTotal();
                } catch (NumberFormatException e) {
                    System.out.println("User inputted bad character into total time field");
                    if(!t1.isEmpty()) {
                        t1 = t1.substring(0, t1.length() - 1);
                        totalTimerField.setText(t1);
                    } else {
                        totalTimerField.clear();
                    }
                }

                getTimer().setTotalTime(totalSeconds);
                updateObjectTree();
            });

            //Setup go to seconds field
            goToSecondsTextField.textProperty().addListener((observable, s, t1) -> {
                try {
                    onSecond = Integer.parseInt(goToSecondsTextField.getText().trim());
                    asisCenteredArc.setArcProgress(onSecond);

                    handelSecondsOverTotal();
                } catch (NumberFormatException e) {
                    System.out.println("User inputted bad character into goto second field");
                    if(!t1.isEmpty()) {
                        t1 = t1.substring(0, t1.length() - 1);
                        goToSecondsTextField.setText(t1);
                    } else {
                        onSecond = 0;
                        asisCenteredArc.setArcProgress(onSecond);
                    }
                }

                setTextAreaVariables();
                setVisibleImage();
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
            if(getTimer().isTimerHidden()) checkBoxHideTimer.setSelected(true);
            if(getTimer().isTimeHidden()) checkBoxHideTime.setSelected(true);

            //Update Tree View
            updateObjectTree();
        });
    }

    private void addCheckBoxFieldListeners() {
        checkBoxHideTime.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            getTimer().setTimeHidden(newValue);
            asisCenteredArc.hideProgress(newValue);
        });

        checkBoxHideTimer.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            getTimer().setTimerHidden(newValue);
            asisCenteredArc.getArcPane().setVisible(!newValue);
        });
    }

    private void addBeatFieldListeners() {
        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                double pitch = Double.parseDouble(t1);
                getTimer().getLine(onSecond).setChangeBeatPitch(pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if(t1.isEmpty()) {
                    getTimer().getLine(onSecond).setChangeBeatPitch(null);
                    textFieldBeatPitch.clear();
                    updateObjectTree();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatPitch.setText(t1);
                updateObjectTree();
            }
        });

        //Setup beat fields
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat speed
            try {
                int speed = Integer.parseInt(t1);
                getTimer().getLine(onSecond).setChangeBeatSpeed(speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if(t1.isEmpty()) {
                    getTimer().getLine(onSecond).setChangeBeatSpeed(null);
                    textFieldBeatSpeed.clear();
                    updateObjectTree();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatSpeed.setText(t1);
                updateObjectTree();
            }
        });
    }

    private void updateObjectTree() {
        TreeItem<String> root = new TreeItem<>("Timer");
        getObjectTree().setRoot(root);

        JSONObject timerObject = getTimer().getTimerAsJson().getJSONObject(0);

        if(timerObject != null && !timerObject.isEmpty()) {
            for (int i = 0; i < timerObject.names().length(); i++) {
                String key = timerObject.names().getString(i);
                Object object = timerObject.get(timerObject.names().getString(i));
                if(object instanceof JSONArray) {
                    TreeItem<String> item = new TreeItem<>(key);
                    root.getChildren().add(item);

                    JSONObject lineObject = ((JSONArray) object).getJSONObject(0);

                    for (int ii = 0; ii < lineObject.names().length(); ii++) {
                        String subKey = lineObject.names().getString(ii);
                        String subValue = lineObject.get(lineObject.names().getString(ii)).toString();

                        TreeItem<String> subItem = new TreeItem<>(subKey+": "+subValue);
                        item.getChildren().add(subItem);
                    }
                    continue;
                }

                final String value = timerObject.get(timerObject.names().getString(i)).toString();
                root.getChildren().add(new TreeItem<>(key+": "+value));
            }
        }

        root.getChildren().sort(Comparator.comparing(t->t.getValue().length()));
        root.setExpanded(true);
    }

    public void actionAddImage() {
        File file = AsisUtils.imageFileChooser();

        if(file != null) {
            //Add image to json object
            if(getScene(getTimer()) != null) {
                getScene(getTimer()).setSceneImage(file);

                setVisibleImage();
            }
        }
    }

    public void actionAddLineImage() {
        File file = AsisUtils.imageFileChooser();

        if(file != null && getTimer().getLine(onSecond) != null) {
            //Add image to json object
            getTimer().getLine(onSecond).setLineImage(file);

            setVisibleImage();
        }
    }

    public void setVisibleImage() {
        //Remove image if any is present
        if(viewPane != null) {
            timerStackPane.getChildren().remove(viewPane);
        }

        File workingFile = getImageFile();

        //Make image visible
        Image image = new Image(workingFile.toURI().toString());
        ImageView sceneImageView = new ImageView();
        sceneImageView.setImage(image);
        sceneImageView.setPreserveRatio(true);
        viewPane.setImageView(sceneImageView);
        timerStackPane.getChildren().add(0, viewPane);
    }

    private File getImageFile() {
        //Create and set working file to passed in var if not null
        File workingFile = new File("");

        //Scene image code
        if(getScene(getTimer()) != null && getScene(getTimer()).getSceneImage() != null) {
            //Set image file
            workingFile = getScene(getTimer()).getSceneImage();

            //Remove add image button
            timerStackPane.getChildren().remove(timerIconControllerBox);
        }

        //Line image code
        if(getTimer().getLine(onSecond) != null && getTimer().getLine(onSecond).getLineImage() != null) {
            //Set image file
            workingFile = getTimer().getLine(onSecond).getLineImage();
        }
        return workingFile;
    }

    private void setTextAreaVariables() {
        setLockTextAreaFunctionality(true);
        timerTextArea.setText("");

        if(getTimer().getLine(onSecond) != null) {
            JSONObject textObject = getTimer().getLine(onSecond).getLineAsJson().getJSONObject(0);
            setFieldsIfNeeded(textObject);

            beatConfiguration(textObject);
        }

        setLockTextAreaFunctionality(false);
    }

    private void setFieldsIfNeeded(JSONObject textObject) {
        if (textObject.has("fillColor")) {
            textColorPicker.setValue(Color.web(textObject.getString("fillColor")));
        }

        if (textObject.has("outlineColor")) {
            textOutlineColorPicker.setValue(Color.web(textObject.getString("outlineColor")));
        }

        if (textObject.has("text")) {
            String text = textObject.getString("text").replaceAll("#", "\n");
            timerTextArea.setText(text);
        }
    }

    private void beatConfiguration(JSONObject textObject) {
        beatProperties(textObject, checkBoxStartBeat, "startBeat");
        beatProperties(textObject, checkBoxStopBeat, "stopBeat");

        changeBeatSpeed(textObject, textFieldBeatSpeed, "changeBeatSpeed");
        changeBeatSpeed(textObject, textFieldBeatPitch, "changeBeatPitch");
    }

    private void handelSecondsOverTotal() {
        if(onSecond > totalSeconds) {
            warningLabel.setVisible(true);
            asisCenteredArc.setArcStrokeColor(Color.RED);
        } else {
            warningLabel.setVisible(false);
            asisCenteredArc.setArcStrokeColor(Color.GREEN);
        }
    }

    public void actionStartBeat() {
        setLineStartCheckBoxState(getTimer().getLine(onSecond), checkBoxStartBeat);
        updateObjectTree();
    }

    public void actionStopBeat() {
        setLineStopCheckBoxState(getTimer().getLine(onSecond), checkBoxStopBeat);
        updateObjectTree();
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
