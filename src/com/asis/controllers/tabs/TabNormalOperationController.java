package com.asis.controllers.tabs;

import com.asis.joi.model.entities.Line;
import com.asis.joi.model.entities.LineGroup;
import com.asis.joi.model.entities.SceneImage;
import com.asis.ui.ImageViewPane;
import com.asis.ui.NumberField;
import com.asis.utilities.AsisUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.File;
import java.util.NoSuchElementException;

import static com.asis.utilities.AsisUtils.colorToHex;

public class TabNormalOperationController extends TabController {
    private String outlineColor = "#000000";
    private String fillColor = "#ffffff";
    private int totalLines = 1;
    private int onLine = 0;

    private final ImageViewPane viewPane = new ImageViewPane();
    private LineGroup lineGroup;

    @FXML
    private TextArea textTextField, mainTextArea;
    @FXML
    private ColorPicker textColorPicker, textOutlineColorPicker;
    @FXML
    private VBox iconControllerBox;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label lineCounterLabel;
    @FXML
    private Button deleteLineButton, previousLineButton;
    @FXML
    private CheckBox checkBoxStopBeat, checkBoxStartBeat, checkStopAmbience;

    @FXML
    private NumberField textFieldBeatPitch, textFieldBeatSpeed, imageSpeedMultiplier;

    public TabNormalOperationController(String tabTitle, LineGroup lineGroup) {
        super(tabTitle);

        setLineGroup(lineGroup);

        Platform.runLater(() -> {
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);

            mainTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                //if(onLine == 0) actionNextLine();

                String text = newValue.trim().replaceAll("\\n", "#");
                getLineGroup().getLine(onLine).setText(text);
            });
            textTextField.textProperty().bindBidirectional(mainTextArea.textProperty());

            addColorPickerListeners();

            addBeatFieldListeners();

            try {
                if(getScene(getLineGroup()).getComponent(SceneImage.class).getFrameRate() != -1) {
                    imageSpeedMultiplier.setVisible(true);
                    imageSpeedMultiplier.setManaged(true);
                    imageSpeedMultiplier.textProperty().addListener((observableValue, s, t1) ->
                            getLineGroup().getLine(onLine).setFrameRateMultiplier(imageSpeedMultiplier.getDoubleNumber()));
                }
            } catch (NoSuchElementException ignored) {}

            //Set total lines
            totalLines = getLineGroup().getLineArrayList().size();

            //Fix extra line bug
            if (totalLines <= 0) {
                totalLines = 1;
            }

            //Set first line text
            setLineVariables();

            //Set the visible image
            setVisibleImage();

            //Set line counter
            lineCounterLabel.setText((onLine+1) + "/" + totalLines);
        });
    }

    private void addColorPickerListeners() {
        textOutlineColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            outlineColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getLineGroup().getLine(onLine).setOutlineColor(outlineColor);
        });

        textColorPicker.valueProperty().addListener((observableValue, color, t1) -> {
            fillColor = removeLastTwoLetters("#" + colorToHex(t1));
            setNodeColorStyle(mainTextArea, fillColor, outlineColor);
            getLineGroup().getLine(onLine).setFillColor(fillColor);
        });
    }

    private void addBeatFieldListeners() {
        //Process beat pitch
        textFieldBeatPitch.textProperty().addListener((observableValue, s, t1) ->
                getLineGroup().getLine(onLine).setChangeBeatPitch(textFieldBeatPitch.getDoubleNumber()));

        //Process beat speed
        textFieldBeatSpeed.textProperty().addListener((observableValue, s, t1) ->
                getLineGroup().getLine(onLine).setChangeBeatSpeed(textFieldBeatSpeed.getIntegerNumber()));
    }

    public void actionAddImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            if (getScene(getLineGroup()) != null) {
                SceneImage image = new SceneImage();
                image.setImage(file);
                getScene(getLineGroup()).addComponent(image);
                setVisibleImage();
            }
        }
    }

    public void actionAddLineImage() {
        File file = AsisUtils.imageFileChooser();

        if (file != null) {
            //Add image to json object
            getLineGroup().getLine(onLine).setLineImage(file);

            setVisibleImage();
        }
    }

    public void actionPreviousLine() {
        if (onLine <= 0) onLine = 0;
        else onLine--;

        if (onLine <= 0) {
            previousLineButton.setDisable(true);
            deleteLineButton.setDisable(true);
        }

        setLineVariables();
        setVisibleImage();

        lineCounterLabel.setText((onLine+1) + "/" + totalLines);
    }

    public void actionNextLine() {
        if ((onLine+1) == totalLines) totalLines++;
        onLine++;

        setLineVariables();
        setVisibleImage();

        lineCounterLabel.setText((onLine+1) + "/" + totalLines);

        deleteLineButton.setDisable(false);
        previousLineButton.setDisable(false);
    }

    public void actionDeleteLine() {
        getLineGroup().removeLine(onLine);

        totalLines--;
        actionPreviousLine();
    }

    public void actionStartBeat() {
        setLineStartCheckBoxState(getLineGroup().getLine(onLine), checkBoxStartBeat);
    }

    public void actionStopBeat() {
        setLineStopCheckBoxState(getLineGroup().getLine(onLine), checkBoxStopBeat);
    }

    public void actionStopAmbience() {
        if(checkStopAmbience.isSelected())
            getLineGroup().getLine(onLine).setStopAmbience(checkStopAmbience.isSelected());
        else
            getLineGroup().getLine(onLine).setStopAmbience(null);
    }

    private void setLineVariables() {
        initializeText();

        checkStopAmbience.setSelected(getLineGroup().getLine(onLine).getStopAmbience() != null);

        JSONObject textObject = getLineGroup().getLine(onLine).toJSON().getJSONObject(0);
        textObjectElseIf();

        beatProperties(textObject, checkBoxStopBeat, "stopBeat");
        beatProperties(textObject, checkBoxStartBeat, "startBeat");

        changeBeatSpeed(textObject, textFieldBeatPitch, "changeBeatPitch");
        changeBeatSpeed(textObject, textFieldBeatSpeed, "changeBeatSpeed");
    }

    private void textObjectElseIf() {
        Line workingLine = getLineGroup().getLine(onLine);
        if(workingLine.getFrameRateMultiplier() != null)
            imageSpeedMultiplier.setText(workingLine.getFrameRateMultiplier().toString());
        else
            imageSpeedMultiplier.clear();

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
        if (getScene(getLineGroup()) != null && getScene(getLineGroup()).hasComponent(SceneImage.class) &&
                getScene(getLineGroup()).getComponent(SceneImage.class).getImage() != null) {
            workingFile = getScene(getLineGroup()).getComponent(SceneImage.class).getImage();
            stackPane.getChildren().remove(iconControllerBox);
        }

        //Line image code
        if (getLineGroup().getLine(onLine) != null && getLineGroup().getLine(onLine).getLineImage() != null) {
            //Set image file
            workingFile = getLineGroup().getLine(onLine).getLineImage();
        }
        return workingFile;
    }

    private void initializeText() {
        if (getLineGroup().getLine(onLine) == null) {
            getLineGroup().addNewLine(onLine);
            getLineGroup().getLine(onLine).setFillColor(fillColor);
            getLineGroup().getLine(onLine).setOutlineColor(outlineColor);
        }
    }

    public void setVisibleImage() {
        super.setVisibleImage(stackPane, viewPane, getImageFile(), getScene(getLineGroup()));
    }

    //Getters and setters
    public LineGroup getLineGroup() {
        return lineGroup;
    }
    public void setLineGroup(LineGroup lineGroup) {
        this.lineGroup = lineGroup;
    }
}
