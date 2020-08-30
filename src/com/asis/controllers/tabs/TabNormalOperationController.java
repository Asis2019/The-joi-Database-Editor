package com.asis.controllers.tabs;

import com.asis.joi.model.entities.Line;
import com.asis.joi.model.entities.LineGroup;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneImage;
import com.asis.ui.ImageViewPane;
import com.asis.utilities.AsisUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.File;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabNormalOperationController extends TabController {
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalLines = 1;
    private int onLine = 1;

    private final ImageViewPane viewPane = new ImageViewPane();
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
                getScene().getComponent(LineGroup.class).getLine(onLine - 1).setText(text);
            });

            textTextField.textProperty().bindBidirectional(mainTextArea.textProperty());

            addColorPickerListeners();

            addBeatFieldListeners();


            //Set total lines
            totalLines = getScene().getComponent(LineGroup.class).getLineArrayList().size();

            //Fix extra line bug
            if (totalLines <= 0) {
                totalLines = 1;
            }

            //Set first line text
            setLineVariables();

            //Set the visible image
            setVisibleImage();

            //Set line counter
            lineCounterLabel.setText(onLine + "/" + totalLines);
        });
    }

    private void addColorPickerListeners() {
        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getScene().getComponent(LineGroup.class).getLine(onLine - 1).setOutlineColor(outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getScene().getComponent(LineGroup.class).getLine(onLine - 1).setFillColor(fillColor);
        });
    }

    private void addBeatFieldListeners() {
        //Setup beat fields
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat pitch
            try {
                final double pitch = Double.parseDouble(t1);
                getScene().getComponent(LineGroup.class).getLine(onLine - 1).setChangeBeatPitch(pitch);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat pitch");
                if (t1.isEmpty()) {
                    getScene().getComponent(LineGroup.class).getLine(onLine - 1).setChangeBeatPitch(null);
                    textFieldBeatPitch.clear();
                    return;
                }
                final String backspacedText = t1.substring(0, t1.length() - 1);
                textFieldBeatPitch.setText(backspacedText);
            }
        });

        //Setup beat fields
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) -> {
            //Process beat speed
            try {
                final int speed = Integer.parseInt(t1);
                getScene().getComponent(LineGroup.class).getLine(onLine - 1).setChangeBeatSpeed(speed);
            } catch (NumberFormatException e) {
                System.out.println("User put bad value into beat speed");
                if (t1.isEmpty()) {
                    getScene().getComponent(LineGroup.class).getLine(onLine - 1).setChangeBeatSpeed(null);
                    textFieldBeatSpeed.clear();
                    return;
                }
                final String backspacedText = t1.substring(0, t1.length() - 1);
                textFieldBeatSpeed.setText(backspacedText);
            }
        });
    }

    public void actionAddImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            //Add image to json object
            SceneImage image = new SceneImage();
            image.setImage(file);
            getScene().addComponent(image);

            setVisibleImage();
        }
    }

    public void actionAddLineImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            //Add image to json object
            getScene().getComponent(LineGroup.class).getLine(onLine - 1).setLineImage(file);

            setVisibleImage();
        }
    }

    public void actionPreviousLine() {
        if (onLine <= 1) onLine = 1;
        else onLine--;

        if (onLine <= 1) {
            previousLineButton.setDisable(true);
            deleteLineButton.setDisable(true);
        }

        setLineVariables();
        setVisibleImage();


        lineCounterLabel.setText(onLine + "/" + totalLines);
    }

    public void actionNextLine() {
        if (onLine == totalLines) totalLines++;
        onLine++;

        setLineVariables();
        setVisibleImage();

        lineCounterLabel.setText(onLine + "/" + totalLines);

        deleteLineButton.setDisable(false);
        previousLineButton.setDisable(false);
    }

    public void actionDeleteLine() {
        getScene().getComponent(LineGroup.class).removeLine(onLine - 1);

        totalLines--;
        actionPreviousLine();
    }

    public void actionStartBeat() {
        setLineStartCheckBoxState(getScene().getComponent(LineGroup.class).getLine(onLine - 1), checkBoxStartBeat);
    }

    public void actionStopBeat() {
        setLineStopCheckBoxState(getScene().getComponent(LineGroup.class).getLine(onLine - 1), checkBoxStopBeat);
    }

    private void setLineVariables() {
        initializeText();

        JSONObject textObject = getScene().getComponent(LineGroup.class).getLine(onLine - 1).toJSON().getJSONObject(0);
        textObjectElseIf();

        beatProperties(textObject, checkBoxStopBeat, "stopBeat");
        beatProperties(textObject, checkBoxStartBeat, "startBeat");

        changeBeatSpeed(textObject, textFieldBeatPitch, "changeBeatPitch");
        changeBeatSpeed(textObject, textFieldBeatSpeed, "changeBeatSpeed");
    }

    private void textObjectElseIf() {
        Line workingLine = getScene().getComponent(LineGroup.class).getLine(onLine - 1);
        if (workingLine.getFillColor() != null)
            textColorPicker.setValue(Color.web(workingLine.getFillColor()));

        if (workingLine.getOutlineColor() != null)
            textOutlineColorPicker.setValue(Color.web(workingLine.getOutlineColor()));

        mainTextArea.setText(workingLine.getText().replaceAll("#", "\n"));
        if (workingLine.getStartBeat() != null) checkBoxStartBeat.setSelected(workingLine.getStartBeat());
    }

    private File getImageFile() {
        //Create and set working file to passed in var if not null
        File workingFile = new File("");

        //Scene image code
        if (getScene().hasComponent(SceneImage.class) && getScene().getComponent(SceneImage.class).getImage() != null) {
            //check if file exists
            if (getScene().getComponent(SceneImage.class).getImage().exists()) {
                //Set image file
                workingFile = getScene().getComponent(SceneImage.class).getImage();

                //Remove add image button
                stackPane.getChildren().remove(iconControllerBox);
            } else {
                System.out.println("Scene titled: " + scene.getComponentTitle() + ", contains and invalid scene image.");
            }
        }

        //Line image code
        if (getScene().getComponent(LineGroup.class).getLine(onLine - 1).getLineImage() != null) {
            //Set image file
            workingFile = getScene().getComponent(LineGroup.class).getLine(onLine - 1).getLineImage();
        }
        return workingFile;
    }

    private void initializeText() {
        if (getScene().getComponent(LineGroup.class).getLine(onLine - 1) == null) {
            getScene().getComponent(LineGroup.class).addNewLine(onLine - 1);
            getScene().getComponent(LineGroup.class).getLine(onLine - 1).setFillColor(fillColor);
            getScene().getComponent(LineGroup.class).getLine(onLine - 1).setOutlineColor(outlineColor);
        }
    }

    public void setVisibleImage() {
        super.setVisibleImage(stackPane, viewPane, getImageFile());
    }

    //Getters and setters
    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
