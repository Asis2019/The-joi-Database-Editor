package com.asis.controllers;

import com.asis.Story;
import com.asis.ui.ImageViewPane;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.soap.Text;
import java.io.File;
import java.util.Optional;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabNormalOperationController extends TabController {
    private Story story;
    private int sceneId;
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalLines = 1;
    private int onLine = 1;

    private ImageViewPane viewPane = new ImageViewPane();

    @FXML protected TextArea textTextField, mainTextArea;
    @FXML protected TextField textFieldBeatPitch, textFieldBeatSpeed;
    @FXML protected ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML protected VBox iconControllerBox;
    @FXML protected StackPane stackPane;
    @FXML protected Label lineCounterLabel;
    @FXML protected Button deleteLineButton, previousLineButton;
    @FXML protected CheckBox checkBoxStopBeat, checkBoxStartBeat;

    public void initialize() {
        mainTextArea.setStyle("outline-color: "+outlineColor+"; fill-color: "+fillColor+";");

        mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            String text = newValue.trim().replaceAll("\\n", "#");
            story.addLine(sceneId, onLine-1, text);
        });

        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
            mainTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToLineObject(sceneId, onLine-1, "outlineColor", outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#"+colorToHex(t1));
            mainTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToLineObject(sceneId, onLine-1, "fillColor", fillColor);
        });

        textTextField.textProperty().bindBidirectional(mainTextArea.textProperty());

        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                double pitch = Double.parseDouble(t1);
                story.addDataToLineObject(sceneId, onLine-1, "changeBeatPitch", pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if(t1.isEmpty()) {
                    story.removeDataFromLineObject(sceneId, onLine-1, "changeBeatPitch");
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
                story.addDataToLineObject(sceneId, onLine - 1, "changeBeatSpeed", speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if(t1.isEmpty()) {
                    story.removeDataFromLineObject(sceneId, onLine-1, "changeBeatSpeed");
                    textFieldBeatSpeed.clear();
                    return;
                }
                t1 = t1.substring(0, t1.length()-1);
                textFieldBeatSpeed.setText(t1);
            }
        });
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
            //Set total lines
            totalLines = story.getTotalLinesInScene(sceneId);

            //Fix extra line bug
            if(totalLines <= 0) {
                totalLines = 1;
            }

            //Set first line text
            setLineVariables();

            //Set the visible image
            setVisibleImage();

            //Set line counter
            lineCounterLabel.setText(onLine+"/"+totalLines);
        }
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
            story.addDataToLineObject(sceneId, onLine-1, "lineImage", file.getName());
            story.addImage(file);

            setVisibleImage();
        }
    }

    public void actionPreviousLine() {
        if(onLine <= 1) {
            onLine = 1;
        } else {
            onLine--;
        }

        if(onLine <= 1) {
            previousLineButton.setDisable(true);
            deleteLineButton.setDisable(true);
        }

        setLineVariables();
        setVisibleImage();


        lineCounterLabel.setText(onLine+"/"+totalLines);
    }

    public void actionNextLine() {
        if(onLine == totalLines) {
            totalLines++;
        }

        onLine++;

        setLineVariables();
        setVisibleImage();

        lineCounterLabel.setText(onLine+"/"+totalLines);

        deleteLineButton.setDisable(false);
        previousLineButton.setDisable(false);
    }

    public void actionDeleteLine() {
        //See what line we are currently at
        //Button should be disabled if its the first line
        //Remove this line
        //Loop through all lines after this one and reduce there numbers by 1
        //onLine will never be less than 2 since 1 is the lowest and the button is disabled at 1


        story.removeDataFromScene(sceneId, "line"+(onLine-1));

        int startingLine = onLine;
        JSONObject tempStory = story.getSceneObject(sceneId);

        if(tempStory != null) {
            //Move all lines down 1
            while (tempStory.has("line" + startingLine)) {

                JSONArray lineObject = tempStory.getJSONArray("line" + startingLine);
                story.addDataToScene(sceneId, "line" + (startingLine-1), lineObject);

                startingLine++;
            }

            //Remove last line
            story.removeDataFromScene(sceneId, "line"+(totalLines-1));
        }

        totalLines--;
        actionPreviousLine();
    }

    public void actionStartBeat() {
        //Add startBeat to story
        if(checkBoxStartBeat.isSelected()) {
            story.addDataToLineObject(sceneId, onLine-1, "startBeat", true);
        } else {
            story.removeDataFromLineObject(sceneId, onLine-1, "startBeat");
        }
    }

    public void actionStopBeat() {
        //Add stopBeat to story
        if(checkBoxStopBeat.isSelected()) {
            story.addDataToLineObject(sceneId, onLine-1, "stopBeat", true);
        } else {
            story.removeDataFromLineObject(sceneId, onLine-1, "stopBeat");
        }
    }

    private void setLineVariables() {
        initializeText();

        JSONObject textObject = story.getLineData(sceneId, onLine-1);

        if (textObject != null) { textObjectElseIf(textObject); }

        beatProperties(textObject, checkBoxStopBeat, "stopBeat");
        beatProperties(textObject, checkBoxStartBeat, "startBeat");

        changeBeat(textObject, textFieldBeatPitch, "changeBeatPitch");
        changeBeat(textObject, textFieldBeatSpeed, "changeBeatSpeed");
    }



    private void textObjectElseIf(JSONObject textObject) {
        if (textObject.has("fillColor")) {
            textColorPicker.setValue(Color.web(textObject.getString("fillColor")));
        }

        if (textObject.has("outlineColor")) {
            textOutlineColorPicker.setValue(Color.web(textObject.getString("outlineColor")));
        }

        if (textObject.has("text")) {
            String text = textObject.getString("text").replaceAll("#", "\n");
            mainTextArea.setText(text);
        }

        if (textObject.has("startBeat")) {
            checkBoxStartBeat.setSelected(true);
        } else {
            checkBoxStartBeat.setSelected(false);
        }
    }

    void setVisibleImage() {
        //Remove image if any is present
        if(viewPane != null) {
            stackPane.getChildren().remove(viewPane);
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
                    stackPane.getChildren().remove(iconControllerBox);
                }
            }
        }

        //Line image code
        JSONObject textObject = story.getLineData(sceneId, onLine - 1);
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
        stackPane.getChildren().add(0, viewPane);
    }

    private void initializeText() {
        JSONObject textObject = story.getLineData(sceneId, onLine-1);
        if(textObject == null) {
            story.addLine(sceneId, onLine-1, "");
            story.addDataToLineObject(sceneId, onLine-1, "fillColor", fillColor);
            story.addDataToLineObject(sceneId, onLine-1, "outlineColor", outlineColor);
        }
    }
}
