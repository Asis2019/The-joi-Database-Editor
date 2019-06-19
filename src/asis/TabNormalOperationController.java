package asis;

import asis.custom_objects.ImageViewPane;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

import static asis.custom_objects.AsisUtils.colorToHex;

public class TabNormalOperationController {
    private Story story;
    private int sceneId;
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalLines = 1;
    private int onLine = 1;

    private ImageViewPane viewPane = new ImageViewPane();

    @FXML private TextArea textTextField, mainTextArea;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox iconControllerBox;
    @FXML private StackPane stackPane;
    @FXML private Label lineCounterLabel;

    public void initialize() {
        mainTextArea.setStyle("outline-color: "+outlineColor+"; fill-color: "+fillColor+";");

        mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            story.addLine(sceneId, onLine-1, newValue);
            //System.out.println("Text: " + newValue);
        });

        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
            System.out.println(outlineColor);
            mainTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToLineObject(sceneId, onLine-1, "outlineColor", outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#"+colorToHex(t1));
            mainTextArea.setStyle("fill-color: "+fillColor+"; outline-color: "+outlineColor+";");
            story.addDataToLineObject(sceneId, onLine-1, "fillColor", fillColor);
        });

        textTextField.textProperty().bindBidirectional(mainTextArea.textProperty());
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

            //Set first line text
            setTextAreaVariables();

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

        setTextAreaVariables();
        setVisibleImage();


        lineCounterLabel.setText(onLine+"/"+totalLines);
    }

    public void actionNextLine() {
        if(onLine == totalLines) {
            totalLines++;
        }

        onLine++;

        setTextAreaVariables();
        setVisibleImage();

        lineCounterLabel.setText(onLine+"/"+totalLines);
    }

    private void setTextAreaVariables() {
        initializeText();

        JSONObject textObject = story.getLineData(sceneId, onLine-1);

        if(textObject != null) {
            if(textObject.has("fillColor")) {
                textColorPicker.setValue(Color.web(textObject.getString("fillColor")));
            }

            if(textObject.has("outlineColor")) {
                textOutlineColorPicker.setValue(Color.web(textObject.getString("outlineColor")));
            }

            if(textObject.has("text")) {
                mainTextArea.setText(textObject.getString("text"));
            }
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
