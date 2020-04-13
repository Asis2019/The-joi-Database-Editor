package com.asis.ui.asis_node;

import com.asis.joi.components.Scene;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
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
    private Scene scene;
    private SceneNodeMainController sceneNodeMainController;
    private VBox outputContainer = new VBox();
    private VBox inputContainer = new VBox();
    private Label titleLabel = new Label("Title");
    private int sceneId;
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;

    private ReadOnlyBooleanWrapper isBadEnd = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper isGoodEnd = new ReadOnlyBooleanWrapper();

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController, Scene scene) {
        this.scene = scene;
        this.sceneId = sceneId;
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


        translateXProperty().addListener((observableValue, number, t1) -> {
            Bounds borderBounds = getBoundsInParent();
            scene.setLayoutXPosition(borderBounds.getMinX());
        });
        translateYProperty().addListener((observableValue, number, t1) -> {
            Bounds borderBounds = getBoundsInParent();
            scene.setLayoutYPosition(borderBounds.getMinY());
        });
        focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            focusState(newValue);
        });

        initializeVBoxes();
        initializeEndVariables();

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("Default", "normal_output");

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
        inputConnection = new AsisConnectionButton(sceneNodeMainController.getPane(), true, sceneId);
        attachHandlers(inputConnection);

        //Add button to lookup list
        sceneNodeMainController.addInputConnection(inputConnection);

        inputContainer.getChildren().add(inputConnection);
    }

    public AsisConnectionButton createNewOutputConnectionPoint(String labelText, String connectionId) {
        AsisConnectionButton connection = new AsisConnectionButton(sceneNodeMainController.getPane(), false, sceneId);
        attachHandlers(connection);

        Label connectionLabel = new Label(labelText);
        connectionLabel.setTextFill(Color.WHITE);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(connectionLabel, connection);

        //Add button to list
        outputConnections.add(connection);

        connection.setConnectionId(connectionId);

        outputContainer.getChildren().add(hBox);

        return connection;
    }

    public void refreshConnectionCenters() {
        inputConnection.calcCenter();
        for (AsisConnectionButton connectionButton : getOutputButtons()) {
            connectionButton.calcCenter();
        }
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
                inputConnection.setButtonColor("#6392c7ff");
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor("#63c763ff");
            }
        });

        isBadEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isBadEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                inputConnection.setButtonColor("#c76363ff");
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor("#63c763ff");
            }
        });

        isGoodEndProperty().bindBidirectional(scene.goodEndProperty());
        isBadEndProperty().bindBidirectional(scene.badEndProperty());
    }

    //Getters and setters
    public int getSceneId() {
        return sceneId;
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
