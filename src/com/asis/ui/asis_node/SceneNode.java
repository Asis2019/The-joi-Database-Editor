package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.joi.components.Scene;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class SceneNode extends Region {
    //TODO make border pane 0.75 opacity

    private Scene scene;
    private BorderPane borderPane = new BorderPane();
    private SceneNodeMainController sceneNodeMainController;
    private VBox outputContainer = new VBox();
    private VBox inputContainer = new VBox();
    private Label titleLabel = new Label("Title");
    private int sceneId;
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;

    private ReadOnlyDoubleWrapper xPosition = new ReadOnlyDoubleWrapper();
    private ReadOnlyDoubleWrapper yPosition = new ReadOnlyDoubleWrapper();

    private ReadOnlyBooleanWrapper isBadEnd = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper isGoodEnd = new ReadOnlyBooleanWrapper();

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController, Scene scene) {
        this.scene = scene;
        this.sceneId = sceneId;
        this.sceneNodeMainController = sceneNodeMainController;

        borderPane.setUserData("sceneNode");

        initializeVBoxes();

        borderPane.setMinSize(width, height);

        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 25px;"
        );

        borderPane.setStyle(
                "-fx-background-color: #5a5a5a;" +
                "-fx-background-radius: 10;" +
                "-fx-background-insets: 8;" +
                "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                "-fx-opacity: 1;"
        );

        borderPane.setCenter(titleLabel);

        createNewInputConnectionPoint();

        createNewOutputConnectionPoint("Default", "normal_output");

        borderPane.translateXProperty().addListener((observableValue, number, t1) -> {
            Bounds bounds = sceneNodeMainController.getScrollPane().getViewportBounds();
            double lowestXPixelShown = -1 * bounds.getMinX();
            Bounds borderBounds = borderPane.localToScene(borderPane.getLayoutBounds());
            xPosition.set(lowestXPixelShown + borderBounds.getMinX());
        });
        borderPane.translateYProperty().addListener((observableValue, number, t1) -> {
            Bounds bounds = sceneNodeMainController.getScrollPane().getViewportBounds();
            double lowestYPixelShown = -1 * bounds.getMinY() - 70; //Like the other fixed value this should be replaced with a dynamic value
            Bounds borderBounds = borderPane.localToScene(borderPane.getLayoutBounds());
            yPosition.set(lowestYPixelShown + borderBounds.getMinY());
        });

        xPositionProperty().addListener((observableValue, number, t1) -> scene.setLayoutXPosition(t1.doubleValue()));
        yPositionProperty().addListener((observableValue, number, t1) -> scene.setLayoutYPosition(t1.doubleValue()));

        initializeEndVariables();
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

    public void removeOutputConnection() {
        outputContainer.getChildren().remove(outputConnections.size()-1);
        outputConnections.remove(outputConnections.size()-1);
    }

    public void removeAllOutputConnection() {
        for(int i=outputContainer.getChildren().size(); i>1; i--) {
            outputContainer.getChildren().remove(i-1);
        }

        for(int i=outputConnections.size(); i>1; i--) {
            outputConnections.remove(i-1);
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
        borderPane.setRight(outputContainer);

        inputContainer.setAlignment(Pos.CENTER_LEFT);
        borderPane.setLeft(inputContainer);

    }

    public int getSceneId() {
        return sceneId;
    }
    public Pane getPane() {
        return borderPane;
    }

    public String getTitle() {
        return titleLabel.getText();
    }
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private ReadOnlyDoubleProperty xPositionProperty() {
        return this.xPosition.getReadOnlyProperty();
    }
    private ReadOnlyDoubleProperty yPositionProperty() {
        return this.yPosition.getReadOnlyProperty();
    }

    private void initializeEndVariables() {
        isGoodEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if(isGoodEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                inputConnection.setButtonColor("#6392c7ff");
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor("#63c763ff");
            }

            Controller.getInstance().setNewChanges();
        });

        isBadEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if(isBadEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                inputConnection.setButtonColor("#c76363ff");
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                inputConnection.setButtonColor("#63c763ff");
            }

            Controller.getInstance().setNewChanges();
        });

        isGoodEndProperty().bindBidirectional(scene.goodEndProperty());
        isBadEndProperty().bindBidirectional(scene.badEndProperty());
    }

    private void setOutputConnectionsInvisible() {
        for(int i =0; i < outputContainer.getChildren().size(); i++) {
            outputContainer.getChildren().get(i).setDisable(true);
            outputContainer.getChildren().get(i).setVisible(false);
        }
    }
    private void setOutputConnectionsVisible() {
        for(int i =0; i < outputContainer.getChildren().size(); i++) {
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
