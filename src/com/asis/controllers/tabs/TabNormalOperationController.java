package com.asis.controllers.tabs;

import com.asis.controllers.Controller;
import com.asis.joi.components.Scene;
import com.asis.ui.ImageViewPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONObject;

import java.io.File;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabNormalOperationController extends TabController {
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalLines = 1;
    private int onLine = 1;

    private ImageViewPane viewPane = new ImageViewPane();
    private Scene scene;

    @FXML private TextArea textTextField, mainTextArea;
    @FXML private TextField textFieldBeatPitch, textFieldBeatSpeed;
    @FXML private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML private VBox iconControllerBox;
    @FXML private StackPane stackPane;
    @FXML private Label lineCounterLabel;
    @FXML private Button deleteLineButton, previousLineButton;
    @FXML private CheckBox checkBoxStopBeat, checkBoxStartBeat;

    public TabNormalOperationController(String tabTitle, Scene scene) {
        super(tabTitle);

        setScene(scene);

        Platform.runLater(() -> {
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);

            mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                String text = newValue.trim().replaceAll("\\n", "#");
                getScene().getLine(onLine-1).setText(text);
            });

            textTextField.textProperty().bindBidirectional(mainTextArea.textProperty());

            addColorPickerListeners();

            addBeatFieldListeners();


            //Set total lines
            totalLines = getScene().getLineArrayList().size();

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
        });
    }

    private void addColorPickerListeners() {
        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#"+colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getScene().getLine(onLine-1).setOutlineColor(outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#"+colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getScene().getLine(onLine-1).setFillColor(fillColor);
        });
    }

    private void addBeatFieldListeners() {
        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                final double pitch = Double.parseDouble(t1);
                getScene().getLine(onLine-1).setChangeBeatPitch(pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if(t1.isEmpty()) {
                    getScene().getLine(onLine-1).setChangeBeatPitch(null);
                    textFieldBeatPitch.clear();
                    return;
                }
                final String backspacedText = t1.substring(0, t1.length()-1);
                textFieldBeatPitch.setText(backspacedText);
            }
        });

        //Setup beat fields
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat speed
            try {
                final int speed = Integer.parseInt(t1);
                getScene().getLine(onLine-1).setChangeBeatSpeed(speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if(t1.isEmpty()) {
                    getScene().getLine(onLine-1).setChangeBeatSpeed(null);
                    textFieldBeatSpeed.clear();
                    return;
                }
                final String backspacedText = t1.substring(0, t1.length()-1);
                textFieldBeatSpeed.setText(backspacedText);
            }
        });
    }

    public void actionAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Controller.getInstance().getJoiPackage().getPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            getScene().setSceneImage(file);

            setVisibleImage();
        }
    }

    public void actionAddLineImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Controller.getInstance().getJoiPackage().getPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            //Add image to json object
            getScene().getLine(onLine-1).setLineImage(file);

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
        getScene().removeLine(onLine-1);

        totalLines--;
        actionPreviousLine();
    }

    public void actionStartBeat() {
        if(checkBoxStartBeat.isSelected()) {
            getScene().getLine(onLine-1).setStartBeat(true);
        } else {
            getScene().getLine(onLine-1).setStartBeat(null);
        }
    }

    public void actionStopBeat() {
        if(checkBoxStopBeat.isSelected()) {
            getScene().getLine(onLine-1).setStopBeat(true);
        } else {
            getScene().getLine(onLine-1).setStopBeat(null);
        }
    }

    private void setLineVariables() {
        initializeText();

        JSONObject textObject = getScene().getLine(onLine-1).getLineAsJson().getJSONObject(0);

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

    public void setVisibleImage() {
        //Remove image if any is present
        if(viewPane != null) {
            stackPane.getChildren().remove(viewPane);
        }

        //Create and set working file to passed in var if not null
        File workingFile = new File("");

        //Scene image code
        if(getScene().getSceneImage() != null) {
            //check if file exists
            if(getScene().getSceneImage().exists()) {
                //Set image file
                workingFile = getScene().getSceneImage();

                //Remove add image button
                stackPane.getChildren().remove(iconControllerBox);
            } else {
                System.out.println("Scene titled: "+scene.getSceneTitle()+", contains and invalid scene image.");
            }
        }

        //Line image code
        if(getScene().getLine(onLine-1).getLineImage() != null) {
            //Set image file
            workingFile = getScene().getLine(onLine-1).getLineImage();
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
        if(getScene().getLine(onLine-1) == null) {
            getScene().addNewLine(onLine-1);
            getScene().getLine(onLine-1).setFillColor(fillColor);
            getScene().getLine(onLine-1).setOutlineColor(outlineColor);
        }
    }

    //Getters and setters
    public Scene getScene() {
        return scene;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
