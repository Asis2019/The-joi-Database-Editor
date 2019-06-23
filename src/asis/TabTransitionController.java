package asis;

import asis.json.JSONObject;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Optional;

import static asis.custom_objects.AsisUtils.clamp;
import static asis.custom_objects.AsisUtils.colorToHex;

public class TabTransitionController {
    private Story story;
    private int sceneId;
    private String textOutlineColor = "#000000";
    private String textFillColor = "#ffffff";
    private Timeline timeline = new Timeline();

    @FXML private TextField fadeSpeedField, waitTimeField, transitionTextField;
    @FXML private ColorPicker transitionTextColor, transitionTextOutlineColor, transitionFadeColor;
    @FXML private Label transitionTextLabel;
    @FXML private Pane transitionPaneMask;

    public void initialize() {
        transitionTextLabel.textProperty().bindBidirectional(transitionTextField.textProperty());
        transitionTextLabel.setStyle("outline-color: "+textOutlineColor+"; fill-color: "+textFillColor+";");

        transitionTextOutlineColor.valueProperty().addListener((observableValue, color, t1) -> {
                textOutlineColor = removeLastTwoLetters("#"+colorToHex(t1));
                transitionTextLabel.setStyle("fill-color: "+textFillColor+"; outline-color: "+textOutlineColor+";");
                story.addDataToTransition(sceneId, "transitionTextOutlineColor", removeLastTwoLetters("#"+colorToHex(t1)));
        });

        transitionTextColor.valueProperty().addListener((observableValue, color, t1) -> {
            textFillColor = removeLastTwoLetters("#"+colorToHex(t1));
            transitionTextLabel.setStyle("fill-color: "+textFillColor+"; outline-color: "+textOutlineColor+";");
            story.addDataToTransition(sceneId, "transitionTextColor", removeLastTwoLetters("#"+colorToHex(t1)));
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

        initializeFields();
    }

    private void initializeFields() {
        JSONObject transitionObject = story.getTransitionData(sceneId);

        if(transitionObject != null) {
            //Set text
            if (transitionObject.has("transitionText")) {
                transitionTextLabel.setText(transitionObject.getString("transitionText"));
            }

            //set fill color
            if (transitionObject.has("transitionTextColor")) {
                transitionTextColor.setValue(Color.web(transitionObject.getString("transitionTextColor")));
                transitionTextLabel.setTextFill(Color.web(transitionObject.getString("transitionTextColor")));
            }

            //set outline color
            if (transitionObject.has("transitionTextOutlineColor")) {
                transitionTextOutlineColor.setValue(Color.web(transitionObject.getString("transitionTextOutlineColor")));
                transitionTextLabel.setStyle("outline-color: "+transitionObject.getString("transitionTextOutlineColor")+";");
            }

            //set wait time
            if (transitionObject.has("waitTime")) {
                waitTimeField.setText(String.valueOf(transitionObject.getInt("waitTime")));
            }

            //set fade speed
            if (transitionObject.has("fadeSpeed")) {
                double fadeSpeed = transitionObject.getDouble("fadeSpeed");
                double fadeSpeedSeconds = 1 / (fadeSpeed * 60);
                fadeSpeedField.setText(String.valueOf(fadeSpeedSeconds));
            }

            //set fade color
            if (transitionObject.has("fadeColor")) {
                transitionFadeColor.setValue(Color.web(transitionObject.getString("fadeColor")));
                transitionPaneMask.setStyle("-fx-background-color: "+transitionObject.getString("fadeColor")+";");
            }
        }
    }

    public void actionTransitionText() {
        //Add transition text to transition object
        String text = transitionTextField.getText().trim();
        story.addDataToTransition(sceneId, "transitionText", text);
    }

    public void actionTransitionWaitTime() {
        //Add transition wait time to transition object
        story.addDataToTransition(sceneId, "waitTime", Integer.valueOf(waitTimeField.getText().trim()));
    }

    public void actionTransitionFadeSpeed() {
        //Add transition fade speed to transition object
        try {
            double fadeSpeedSeconds = Double.parseDouble(fadeSpeedField.getText().trim());
            double fadeSpeed = 1 / (fadeSpeedSeconds * 60);
            story.addDataToTransition(sceneId, "fadeSpeed", clamp(fadeSpeed, 0.0000000001, 5));
        } catch (NumberFormatException e) {
            System.out.println("Incorrect value entered");
        }
    }

    public void actionFadeColor() {
        transitionPaneMask.setStyle("-fx-background-color: #"+ colorToHex(transitionFadeColor.getValue())+";");


        String color = "#"+colorToHex(transitionFadeColor.getValue());
        story.addDataToTransition(sceneId, "fadeColor", color);
    }

    public void actionPlayTransitionButton() {
        int waitTimeFieldValue = 0; //Default 0 seconds

        try {
            waitTimeFieldValue = Integer.valueOf(waitTimeField.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("Caught error: "+e.getMessage());
        }

        double fadeSpeedFieldValue = 1d; //Default 1 second

        try {
            fadeSpeedFieldValue = Double.valueOf(fadeSpeedField.getText().trim()); //game default is 0.02 must be converted from number
        } catch (NumberFormatException e) {
            System.out.println("Caught error: "+e.getMessage());
        }

        timeline.setOnFinished(e -> {
            transitionPaneMask.setOpacity(0);
            transitionTextLabel.setOpacity(1);
        });

        if(timeline.getCurrentRate() == 0.0d) {
            //Remove old keyFrames
            timeline.getKeyFrames().clear();

            //Fade in
            KeyValue paneOpacity = new KeyValue(transitionPaneMask.opacityProperty(), 0.0);
            KeyValue textOpacity = new KeyValue(transitionTextLabel.opacityProperty(), 0.0);
            KeyFrame start = new KeyFrame(Duration.ZERO, paneOpacity, textOpacity);

            //Faded in point
            KeyValue paneOpacity2 = new KeyValue(transitionPaneMask.opacityProperty(), 1.0);
            KeyValue textOpacity2 = new KeyValue(transitionTextLabel.opacityProperty(), 1.0);
            KeyFrame middle1 = new KeyFrame(Duration.seconds(fadeSpeedFieldValue), paneOpacity2, textOpacity2);

            //Faded in point
            KeyValue paneOpacity3 = new KeyValue(transitionPaneMask.opacityProperty(), 1.0);
            KeyValue textOpacity3 = new KeyValue(transitionTextLabel.opacityProperty(), 1.0);
            KeyFrame middle2 = new KeyFrame(Duration.seconds(fadeSpeedFieldValue+waitTimeFieldValue), paneOpacity3, textOpacity3);

            //Fade out
            KeyValue paneOpacity4 = new KeyValue(transitionPaneMask.opacityProperty(), 0.0);
            KeyValue textOpacity4 = new KeyValue(transitionTextLabel.opacityProperty(), 0.0);
            KeyFrame end = new KeyFrame(Duration.seconds(fadeSpeedFieldValue+fadeSpeedFieldValue+waitTimeFieldValue), paneOpacity4, textOpacity4);

            timeline.getKeyFrames().addAll(start, middle1, middle2, end);
            timeline.play();
        }
    }
}
