package asis;

import asis.custom_objects.AsisCenteredArc;
import asis.custom_objects.ImageViewPane;
import asis.json.JSONObject;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

import static asis.custom_objects.AsisUtils.colorToHex;

public class TabTimerController {
    private Story story;
    private int sceneId;
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalSeconds = 0;
    private int onSecond = 0;

    private ImageViewPane viewPane = new ImageViewPane();
    private AsisCenteredArc asisCenteredArc = new AsisCenteredArc();

    @FXML private TextField goToSecondsTextField, totalTimerField, textFieldBeatPitch, textFieldBeatSpeed;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox timerIconControllerBox;
    @FXML private StackPane timerStackPane;
    @FXML private TextArea timerTextArea, textTextArea;
    @FXML private HBox container;
    @FXML private CheckBox checkBoxStopBeat, checkBoxStartBeat;

    public void initialize() {
        timerTextArea.setStyle("outline-color: "+outlineColor+"; fill-color: "+fillColor+";");

        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
            timerTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToTimerLineObject(sceneId, "line"+onSecond, "outlineColor", outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#"+colorToHex(t1));
            timerTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToTimerLineObject(sceneId, "line"+onSecond, "fillColor", fillColor);
        });

        textTextArea.textProperty().bindBidirectional(timerTextArea.textProperty());

        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                double pitch = Double.parseDouble(t1);
                story.addDataToTimerLineObject(sceneId, "line"+onSecond, "changeBeatPitch", pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if(t1.isEmpty()) {
                    story.removeDataFromTimerLineObject(sceneId, "line"+onSecond, "changeBeatPitch");
                    textFieldBeatPitch.clear();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatPitch.setText(t1);
            }
        });

        //Setup beat fields
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat speed
            try {
                int speed = Integer.parseInt(t1);
                story.addDataToTimerLineObject(sceneId, "line"+onSecond, "changeBeatSpeed", speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if(t1.isEmpty()) {
                    story.removeDataFromTimerLineObject(sceneId, "line"+onSecond, "changeBeatSpeed");
                    textFieldBeatSpeed.clear();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatSpeed.setText(t1);
            }
        });

        //timer
        asisCenteredArc.setMaxLength(0);
        asisCenteredArc.setArcProgress(0);
        container.getChildren().add(asisCenteredArc.getArcPane());
    }

    private String removeLastTwoLetters(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 2))
                .orElse(s);
    }

    void passData(Story story, int sceneId) {
        this.story = story;
        this.sceneId = sceneId;

        //Set image if already set in scene
        if(story != null) {
            //Set text area
            setTextAreaVariables();

            //Set the visible image
            setVisibleImage();

            //total timer
            JSONObject timerObject = story.getTimerData(sceneId);

            if(timerObject != null) {
                if(timerObject.has("totalTime")) {
                    totalTimerField.setText(String.valueOf(timerObject.getInt("totalTime")));
                    totalSeconds = Integer.parseInt(totalTimerField.getText().trim());
                    asisCenteredArc.setMaxLength(totalSeconds);
                }
            }
        }
    }

    public void actionTextTyped() {
        String text = textTextArea.getText().trim().replaceAll("\\n", "#");
        story.addDataToTimerLineObject(sceneId, "line"+onSecond, "text", text);
    }

    public void actionAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(story.getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            story.addDataToScene(sceneId, "sceneImage", file.getName());
            story.addImage(file);

            setVisibleImage();
        }
    }

    public void actionAddLineImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(story.getProjectDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            story.addDataToTimerLineObject(sceneId, "line"+onSecond, "lineImage", file.getName());
            story.addImage(file);

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
        String sceneImage = story.getSceneImage(sceneId);
        if (sceneImage != null) {
            for (File imageFiles : story.getImagesArray()) {
                if(imageFiles.getName().equals(sceneImage)) {
                    workingFile = imageFiles;

                    //Remove add image button
                    timerStackPane.getChildren().remove(timerIconControllerBox);
                }
            }
        }

        //Line image code
        JSONObject textObject = story.getTimerLineData(sceneId, "line"+onSecond);
        if (textObject != null) {
            if (textObject.has("lineImage")) {
                String lineImage = textObject.getString("lineImage");
                if (lineImage != null) {
                    for (File imageFiles : story.getImagesArray()) {
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

    public void actionGoToSecondsField() {
        try {
            onSecond = Integer.parseInt(goToSecondsTextField.getText().trim());
            asisCenteredArc.setArcProgress(onSecond);
        } catch (NumberFormatException e) {
            System.out.println("User inputted bad character into goto second field");
            String t1 = goToSecondsTextField.getText();
            if(!t1.isEmpty()) {
                t1 = t1.substring(0, t1.length() - 1);
                goToSecondsTextField.setText(t1);
            } else {
                onSecond = 0;
                asisCenteredArc.setArcProgress(onSecond);
            }
        }

        setTextAreaVariables();
    }

    private void setTextAreaVariables() {
        timerTextArea.setText("");

        JSONObject textObject = story.getTimerLineData(sceneId, "line"+onSecond);

        if(textObject != null) {
            if(textObject.has("fillColor")) {
                textColorPicker.setValue(Color.web(textObject.getString("fillColor")));
            }

            if(textObject.has("outlineColor")) {
                textOutlineColorPicker.setValue(Color.web(textObject.getString("outlineColor")));
            }

            if(textObject.has("text")) {
                String text = textObject.getString("text").replaceAll("#", "\n");
                timerTextArea.setText(text);
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

    public void actionTotalTimerField() {
        try {
            totalSeconds = Integer.parseInt(totalTimerField.getText().trim());
            asisCenteredArc.setMaxLength(totalSeconds);
        } catch (NumberFormatException e) {
            System.out.println("User inputted bad character into total time field");
            String t1 = totalTimerField.getText();
            t1 = t1.substring(0, t1.length()-1);
            totalTimerField.setText(t1);
        }

        story.addDataToTimerObject(sceneId, totalSeconds);
    }

    public void actionStartBeat() {
        //Add startBeat to story
        if(checkBoxStartBeat.isSelected()) {
            story.addDataToTimerLineObject(sceneId, "line"+onSecond, "startBeat", true);
        } else {
            story.removeDataFromTimerLineObject(sceneId, "line"+onSecond, "startBeat");
        }
    }

    public void actionStopBeat() {
        //Add stopBeat to story
        if(checkBoxStopBeat.isSelected()) {
            story.addDataToTimerLineObject(sceneId, "line"+onSecond, "stopBeat", true);
        } else {
            story.removeDataFromTimerLineObject(sceneId, "line"+onSecond, "stopBeat");
        }
    }
}
