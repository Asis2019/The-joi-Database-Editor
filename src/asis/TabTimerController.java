package asis;

import asis.custom_objects.AsisCenteredArc;
import asis.custom_objects.ImageViewPane;
import asis.json.JSONObject;
import javafx.fxml.FXML;
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

    @FXML private TextField goToSecondsTextField, totalTimerField;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox timerIconControllerBox;
    @FXML private StackPane timerStackPane;
    @FXML private TextArea timerTextArea, textTextArea;
    @FXML private HBox container;

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
            onSecond = Integer.valueOf(goToSecondsTextField.getText().trim());
            asisCenteredArc.setArcProgress(onSecond);
        } catch (NumberFormatException e) {
            System.out.println("Error: "+e.getMessage());
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
        }
    }

    public void actionTotalTimerField() {
        try {
            totalSeconds = Integer.valueOf(totalTimerField.getText().trim());
            asisCenteredArc.setMaxLength(totalSeconds);
        } catch (NumberFormatException e) {
            System.out.println("Error: "+e.getMessage());
        }

        story.addDataToTimerObject(sceneId, totalSeconds);
    }
}
