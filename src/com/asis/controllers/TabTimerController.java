package com.asis.controllers;

import com.asis.Story;
import com.asis.ui.AsisCenteredArc;
import com.asis.ui.ImageViewPane;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabTimerController {
    private int sceneId;
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalSeconds = 0;
    private int onSecond = 0;
    private boolean lockTextAreaFunctionality = false;

    private ImageViewPane viewPane = new ImageViewPane();
    private AsisCenteredArc asisCenteredArc = new AsisCenteredArc();

    @FXML private TextField goToSecondsTextField, totalTimerField, textFieldBeatPitch, textFieldBeatSpeed;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox timerIconControllerBox;
    @FXML private StackPane timerStackPane;
    @FXML private TextArea timerTextArea, textTextArea;
    @FXML private HBox container;
    @FXML private CheckBox checkBoxStopBeat, checkBoxStartBeat;
    @FXML private Label warningLabel;
    @FXML private TreeView<String> objectTree;

    public void initialize() {
        timerTextArea.setStyle("outline-color: "+outlineColor+"; fill-color: "+fillColor+";");

        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
            timerTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story().addDataToTimerLineObject(sceneId, "line"+onSecond, "outlineColor", outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#"+colorToHex(t1));
            timerTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story().addDataToTimerLineObject(sceneId, "line"+onSecond, "fillColor", fillColor);
        });

        textTextArea.textProperty().bindBidirectional(timerTextArea.textProperty());

        //Setup text area
        textTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!isLockTextAreaFunctionality()) {
                newValue = newValue.replaceAll("\\n", "#");

                if (!newValue.isEmpty()) {
                    story().addDataToTimerLineObject(sceneId, "line" + onSecond, "text", newValue);
                    story().addDataToTimerLineObject(sceneId, "line" + onSecond, "fillColor", fillColor);
                    story().addDataToTimerLineObject(sceneId, "line" + onSecond, "outlineColor", outlineColor);
                } else {
                    story().removeDataFromTimer(sceneId, "line" + onSecond);
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

            story().addDataToTimerObject(sceneId, totalSeconds);
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
        });

        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                double pitch = Double.parseDouble(t1);
                story().addDataToTimerLineObject(sceneId, "line"+onSecond, "changeBeatPitch", pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if(t1.isEmpty()) {
                    story().removeDataFromTimerLineObject(sceneId, "line"+onSecond, "changeBeatPitch");
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
                story().addDataToTimerLineObject(sceneId, "line"+onSecond, "changeBeatSpeed", speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if(t1.isEmpty()) {
                    story().removeDataFromTimerLineObject(sceneId, "line"+onSecond, "changeBeatSpeed");
                    textFieldBeatSpeed.clear();
                    updateObjectTree();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatSpeed.setText(t1);
                updateObjectTree();
            }
        });

        //timer
        asisCenteredArc.setMaxLength(0);
        asisCenteredArc.setArcProgress(0);
        container.getChildren().add(asisCenteredArc.getArcPane());
    }

    private void updateObjectTree() {
        TreeItem<String> root = new TreeItem<>("Timer");
        getObjectTree().setRoot(root);

        JSONObject timerObject = Story.getInstance().getTimerData(sceneId);

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

                String value = timerObject.get(timerObject.names().getString(i)).toString();

                TreeItem<String> item = new TreeItem<>(key+": "+value);
                root.getChildren().add(item);
            }
        }

        root.getChildren().sort(Comparator.comparing(t->t.getValue().length()));
        root.setExpanded(true);
    }

    private String removeLastTwoLetters(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 2))
                .orElse(s);
    }

    void passData(int sceneId) {
        this.sceneId = sceneId;

        //Set image if already set in scene
        if(story() != null) {
            //Set text area
            setTextAreaVariables();

            //Set the visible image
            setVisibleImage();

            //total timer
            JSONObject timerObject = story().getTimerData(sceneId);

            if(timerObject != null) {
                if(timerObject.has("totalTime")) {
                    totalTimerField.setText(String.valueOf(timerObject.getInt("totalTime")));
                    totalSeconds = Integer.parseInt(totalTimerField.getText().trim());
                    asisCenteredArc.setMaxLength(totalSeconds);
                }
            }

            //Update Tree View
            updateObjectTree();
        }
    }

    public void actionAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(story().getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            story().addDataToScene(sceneId, "sceneImage", file.getName());
            story().addImage(file);

            setVisibleImage();
        }
    }

    public void actionAddLineImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(story().getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            story().addDataToTimerLineObject(sceneId, "line"+onSecond, "lineImage", file.getName());
            story().addImage(file);

            setVisibleImage();
        }
    }

    void setVisibleImage() {
        //Remove image if any is present
        if(viewPane != null) {
            timerStackPane.getChildren().remove(viewPane);
        }

        //Create and set working file to passed in var if not null
        File workingFile = new File("");

        //Scene image code
        String sceneImage = story().getSceneImage(sceneId);
        if (sceneImage != null) {
            for (File imageFiles : story().getImagesArray()) {
                if(imageFiles.getName().equals(sceneImage)) {
                    workingFile = imageFiles;

                    //Remove add image button
                    timerStackPane.getChildren().remove(timerIconControllerBox);
                }
            }
        }

        //Line image code
        JSONObject textObject = story().getTimerLineData(sceneId, "line"+onSecond);
        if (textObject != null) {
            if (textObject.has("lineImage")) {
                String lineImage = textObject.getString("lineImage");
                if (lineImage != null) {
                    for (File imageFiles : story().getImagesArray()) {
                        if(imageFiles.getName().equals(lineImage)) {
                            workingFile = imageFiles;
                        }
                    }
                }
            }
        }

        //Make image visible
        Image image = new Image(workingFile.toURI().toString());
        ImageView sceneImageView = new ImageView();
        sceneImageView.setImage(image);
        sceneImageView.setPreserveRatio(true);
        viewPane.setImageView(sceneImageView);
        timerStackPane.getChildren().add(0, viewPane);
    }

    private void setTextAreaVariables() {
        setLockTextAreaFunctionality(true);
        timerTextArea.setText("");

        JSONObject textObject = story().getTimerLineData(sceneId, "line"+onSecond);

        TabNormalOperationController.getInstance().textObjectValidation(textObject);

        setLockTextAreaFunctionality(false);
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
        //Add startBeat to story()
        if(checkBoxStartBeat.isSelected()) {
            story().addDataToTimerLineObject(sceneId, "line"+onSecond, "startBeat", true);
        } else {
            story().removeDataFromTimerLineObject(sceneId, "line"+onSecond, "startBeat");
        }
        updateObjectTree();
    }

    public void actionStopBeat() {
        //Add stopBeat to story()
        if(checkBoxStopBeat.isSelected()) {
            story().addDataToTimerLineObject(sceneId, "line"+onSecond, "stopBeat", true);
        } else {
            story().removeDataFromTimerLineObject(sceneId, "line"+onSecond, "stopBeat");
        }
        updateObjectTree();
    }
    
    private Story story() {
        return Story.getInstance();
    }

    private boolean isLockTextAreaFunctionality() {
        return lockTextAreaFunctionality;
    }

    private void setLockTextAreaFunctionality(boolean lockTextAreaFunctionality) {
        this.lockTextAreaFunctionality = lockTextAreaFunctionality;
    }

    private TreeView<String> getObjectTree() {
        return objectTree;
    }
}
