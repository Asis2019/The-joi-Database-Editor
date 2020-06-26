package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.joi.model.entites.Scene;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class SceneNode extends BorderPane {
    private final Scene scene;
    private SceneNodeMainController sceneNodeMainController;
    private VBox outputContainer = new VBox();
    private VBox inputContainer = new VBox();
    private Label titleLabel = new Label("Title");
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;

    private final ReadOnlyBooleanWrapper isBadEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper isGoodEnd = new ReadOnlyBooleanWrapper();

    public double innerX, innerY;

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController, Scene scene) {
        this.scene = scene;
        this.scene.setSceneId(sceneId);
        this.sceneNodeMainController = sceneNodeMainController;

        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 25px;" +
                        "-fx-focus-color: blue;"
        );

        setUserData("sceneNode");
        setMinSize(width, height);
        setFocusTraversable(true);
        setCenter(titleLabel);

        translateXProperty().addListener((observableValue, number, t1) -> scene.setLayoutXPosition(t1.doubleValue()));
        translateYProperty().addListener((observableValue, number, t1) -> scene.setLayoutYPosition(t1.doubleValue()));
        focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> focusState(newValue));

        initializeVBoxes();

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("Default", "normal_output");

        initializeEndVariables();
        focusState(false);
    }

    private void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    public List<AsisConnectionButton> getOutputButtons() {
        return this.outputConnections;
    }

    public AsisConnectionButton getInputConnection() {
        return this.inputConnection;
    }

    private void createNewInputConnectionPoint() {
        inputConnection = new AsisConnectionButton(sceneNodeMainController.getPane(), true, getSceneId());
        attachHandlers(inputConnection);

        //Add button to lookup list
        sceneNodeMainController.addInputConnection(inputConnection);

        inputContainer.getChildren().add(inputConnection);
    }

    public AsisConnectionButton createNewOutputConnectionPoint(String labelText, String connectionId) {
        AsisConnectionButton connection = new AsisConnectionButton(sceneNodeMainController.getPane(), false, getSceneId());
        attachHandlers(connection);

        Label connectionLabel = new Label(labelText);
        connectionLabel.setTextFill(Color.WHITE);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(connectionLabel, connection);

        //Add button to list
        outputConnections.add(connection);

        connection.setId(connectionId);

        outputContainer.getChildren().add(hBox);

        return connection;
    }

    public void removeOutputConnection() {
        outputContainer.getChildren().remove(outputConnections.size() - 1);
        outputConnections.remove(outputConnections.size() - 1);
    }

    public void removeAllOutputConnection() {
        for (int i = outputContainer.getChildren().size(); i > 1; i--) {
            outputContainer.getChildren().remove(i - 1);
        }

        for (int i = outputConnections.size(); i > 1; i--) {
            outputConnections.remove(i - 1);
        }
    }

    private void attachHandlers(AsisConnectionButton connection) {
        connection.setOnMouseMoved(e -> sceneNodeMainController.mouseMoved(e));
        connection.setOnMouseDragged(e -> sceneNodeMainController.mouseMoved(e));
        connection.setOnMousePressed(e -> sceneNodeMainController.mousePressed(connection));
        connection.setOnMouseReleased(e -> sceneNodeMainController.mouseReleased(e));
    }

    private void initializeVBoxes() {
        outputContainer.setAlignment(Pos.CENTER_RIGHT);
        outputContainer.setSpacing(5);
        outputContainer.setPadding(new Insets(20, 0, 20, 0));
        setRight(outputContainer);

        inputContainer.setAlignment(Pos.CENTER_LEFT);
        setLeft(inputContainer);
    }

    private void initializeEndVariables() {
        isGoodEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isGoodEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                inputConnection.setButtonColor(AsisConnectionButton.GOOD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isBadEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isBadEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                inputConnection.setButtonColor(AsisConnectionButton.BAD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isGoodEndProperty().bindBidirectional(scene.goodEndProperty());
        isBadEndProperty().bindBidirectional(scene.badEndProperty());
    }

    public void positionInGrid(double x, double y) {
        if(Controller.getInstance().isSnapToGrid()) {
            setTranslateX(round(x));
            setTranslateY(round(y));
        } else {
            setTranslateX(x);
            setTranslateY(y);
        }

        innerX = x;
        innerY = y;
    }

    private static double round(double v) {
        final int multiple = 10;
        return multiple * (Math.round(v / multiple));
    }

    //Getters and setters
    public int getSceneId() {
        return this.scene.getSceneId();
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private void setOutputConnectionsInvisible() {
        for (int i = 0; i < outputContainer.getChildren().size(); i++) {
            outputContainer.getChildren().get(i).setDisable(true);
            outputContainer.getChildren().get(i).setVisible(false);
        }
    }

    private void setOutputConnectionsVisible() {
        for (int i = 0; i < outputContainer.getChildren().size(); i++) {
            outputContainer.getChildren().get(i).setDisable(false);
            outputContainer.getChildren().get(i).setVisible(true);
        }
    }

    public boolean isBadEnd() {
        return isBadEnd.get();
    }

    public ReadOnlyBooleanWrapper isBadEndProperty() {
        return isBadEnd;
    }

    public void setIsBadEnd(boolean isBadEnd) {
        this.isBadEnd.set(isBadEnd);
    }

    public boolean isGoodEnd() {
        return isGoodEnd.get();
    }

    public ReadOnlyBooleanWrapper isGoodEndProperty() {
        return isGoodEnd;
    }

    public void setIsGoodEnd(boolean isGoodEnd) {
        this.isGoodEnd.set(isGoodEnd);
    }
}
