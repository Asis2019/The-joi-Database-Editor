package com.asis.controllers.tabs;

import com.asis.joi.model.entities.Transition;
import com.asis.ui.NumberField;
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
    private Transition transition;

    @FXML private TextField transitionTextField;
    @FXML private ColorPicker transitionTextColor, transitionTextOutlineColor, transitionFadeColor;
    @FXML private Label transitionTextLabel;
    @FXML private Pane transitionPaneMask;

    @FXML private NumberField fadeSpeedField, waitTimeField;

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
        setNodeColorStyle(transitionTextLabel, textFillColor, textOutlineColor);

        transitionTextField.textProperty().addListener((observableValue, s, t1) -> getTransition().setTransitionText(t1==null || t1.isEmpty()?null:t1));

        transitionTextOutlineColor.valueProperty().addListener((observableValue, color, t1) -> {
            textOutlineColor = removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1));
            setNodeColorStyle(transitionTextLabel, textFillColor, textOutlineColor);
            getTransition().setTransitionTextOutlineColor(textOutlineColor);
        });

        transitionTextColor.valueProperty().addListener((observableValue, color, t1) -> {
            textFillColor = removeLastTwoLetters("#"+ AsisUtils.colorToHex(t1));
            setNodeColorStyle(transitionTextLabel, textFillColor, textOutlineColor);
            getTransition().setTransitionTextColor(textFillColor);
        });

        //Add transition fade speed to transition object
        fadeSpeedField.textProperty().addListener((observableValue, color, t1) -> getTransition().setFadeSpeed(fadeSpeedField.getDoubleNumber(1d)));

        //Add transition wait time to transition object
        waitTimeField.textProperty().addListener((observableValue, color, t1) -> getTransition().setWaitTime(waitTimeField.getIntegerNumber(0)));
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

    public void actionFadeColor() {
        final String color = removeLastTwoLetters("#"+AsisUtils.colorToHex(transitionFadeColor.getValue()));
        transitionPaneMask.setStyle("-fx-background-color: "+color+";");
        getTransition().setFadeColor(color);
    }

    public void actionPlayTransitionButton() {
        final int waitTimeFieldValue = (int) setVariable(waitTimeField, 0);
        final double fadeSpeedFieldValue = setVariable(fadeSpeedField, 1d); //game default is 0.02 must be converted from number

        createAndRunTimeLine(waitTimeFieldValue, fadeSpeedFieldValue);
    }

    private double setVariable(TextField dataContainer, double defaultValue) {
        try {
            return Double.parseDouble(dataContainer.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("Caught error: "+e.getMessage());
        }
        return defaultValue;
    }

    private void createAndRunTimeLine(final int waitTimeFieldValue, final double fadeSpeedFieldValue) {
        Timeline timeline = new Timeline();
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
