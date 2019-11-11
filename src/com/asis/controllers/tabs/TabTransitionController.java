package com.asis.controllers.tabs;

import com.asis.joi.components.Transition;
import com.asis.utilities.AsisUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TabTransitionController extends TabController {
    private String textOutlineColor = "#000000";
    private String textFillColor = "#ffffff";
    private Timeline timeline = new Timeline();
    private Transition transition;

    @FXML private TextField fadeSpeedField, waitTimeField, transitionTextField;
    @FXML private ColorPicker transitionTextColor, transitionTextOutlineColor, transitionFadeColor;
    @FXML private Label transitionTextLabel;
    @FXML private Pane transitionPaneMask;

    public TabTransitionController(String tabTitle, Transition transition) {
        super(tabTitle);

        setTransition(transition);

        Platform.runLater(() -> {
            setupInitialFieldProperties();

            addInitialDataToFields();
        });
    }

    private void setupInitialFieldProperties() {
        transitionTextLabel.textProperty().bindBidirectional(transitionTextField.textProperty());
        transitionTextLabel.setStyle("outline-color: "+textOutlineColor+"; fill-color: "+textFillColor+";");

        transitionTextOutlineColor.valueProperty().addListener((observableValue, color, t1) -> {
            textOutlineColor = removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1));
            transitionTextLabel.setStyle("fill-color: "+textFillColor+"; outline-color: "+textOutlineColor+";");
            getTransition().setTransitionTextOutlineColor(removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1)));
        });

        transitionTextColor.valueProperty().addListener((observableValue, color, t1) -> {
            textFillColor = removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1));
            transitionTextLabel.setStyle("fill-color: "+textFillColor+"; outline-color: "+textOutlineColor+";");
            getTransition().setTransitionTextColor(removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1)));
        });

        //Add transition fade speed to transition object
        fadeSpeedField.textProperty().addListener((observableValue, color, t1) -> {
            try {
                final double fadeSpeed = Double.parseDouble(t1);
                getTransition().setFadeSpeed(fadeSpeed);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect value entered in FadeSpeed field");
                if(t1.isEmpty()) {
                    getTransition().setFadeSpeed(1);
                    fadeSpeedField.clear();
                    return;
                }

                final String backspacedText = t1.substring(0, t1.length()-1);
                fadeSpeedField.setText(backspacedText);
            }
        });

        //Add transition wait time to transition object
        waitTimeField.textProperty().addListener((observableValue, color, t1) -> {
            try {
                final int waitTime = Integer.parseInt(t1);
                getTransition().setWaitTime(waitTime);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect value entered in waitTime field");
                if(t1.isEmpty()) {
                    getTransition().setWaitTime(0);
                    waitTimeField.clear();
                    return;
                }

                final String backspacedText = t1.substring(0, t1.length()-1);
                waitTimeField.setText(backspacedText);
            }
        });
    }

    private void addInitialDataToFields() {
        //Set text
        transitionTextLabel.setText(getTransition().getTransitionText());

        //set fill color
        transitionTextColor.setValue(Color.web(getTransition().getTransitionTextColor()));

        //set outline color
        transitionTextOutlineColor.setValue(Color.web(getTransition().getTransitionTextOutlineColor()));

        //set wait time
        waitTimeField.setText(getTransition().getWaitTime() == 0 ? "" : String.valueOf(getTransition().getWaitTime()));

        //set fade speed
        fadeSpeedField.setText(getTransition().getFadeSpeed() == 1 ? "" : String.valueOf(getTransition().getFadeSpeed()));

        //set fade color
        if (getTransition().getFadeColor() != null && !getTransition().getFadeColor().isEmpty()) {
            transitionFadeColor.setValue(Color.web(getTransition().getFadeColor()));
            transitionPaneMask.setStyle("-fx-background-color: " + getTransition().getFadeColor() + ";");
        }
    }

    public void actionTransitionText() {
        //Add transition text to transition object
        String text = transitionTextField.getText();
        getTransition().setTransitionText(text==null || text.isEmpty()?null:text);
    }

    public void actionFadeColor() {
        String color = removeLastTwoLetters(AsisUtils.colorToHex(transitionFadeColor.getValue()));
        transitionPaneMask.setStyle("-fx-background-color: #"+color+";");

        color = "#"+color;
        getTransition().setFadeColor(color);
    }

    public void actionPlayTransitionButton() {
        int waitTimeFieldValue = 0; //Default 0 seconds

        try {
            waitTimeFieldValue = Integer.parseInt(waitTimeField.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("Caught error: "+e.getMessage());
        }

        double fadeSpeedFieldValue = 1d; //Default 1 second

        try {
            fadeSpeedFieldValue = Double.parseDouble(fadeSpeedField.getText().trim()); //game default is 0.02 must be converted from number
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

    //Getters and setters
    public Transition getTransition() {
        return transition;
    }
    public void setTransition(Transition transition) {
        this.transition = transition;
    }
}
