package com.asis.ui.asis_node;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class AsisConnectionButton extends Button {
    private ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
    private ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();

    private Pane parentPane;

    //The connections id
    private String id = null;

    //What scene there a part of
    private int parentSceneId;

    //The line object connected
    private BoundLine boundLine;

    //The connection type
    private boolean connectionType; //false is output true is input

    //Option number
    private int optionNumber;

    private static final String outputConnectorStyle =
            "-fx-background-color: #63c763ff, transparent, transparent;" +
            "-fx-background-radius: 5em;" +
            "-fx-border-radius: 5em;" +
            "-fx-min-width: 15px; " +
            "-fx-min-height: 15px; " +
            "-fx-max-width: 15px; " +
            "-fx-max-height: 15px;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 1;" +
            "-fx-background-insets: 1;";

    AsisConnectionButton(Pane parentPane, boolean connectionType, int parentSceneId) {
        this.parentPane = parentPane;
        this.connectionType = connectionType;
        this.parentSceneId = parentSceneId;

        setStyle(outputConnectorStyle);
        setCursor(Cursor.HAND);

        this.localToSceneTransformProperty().addListener((observableValue, transform, t1) -> calcCenter());
    }

    void setBoundLine(BoundLine boundLine) {
        this.boundLine = boundLine;
    }

    void setButtonColor(String hexColor) {
        setStyle("-fx-background-color: "+hexColor+", transparent, transparent;" +
                "-fx-background-radius: 5em;" +
                "-fx-border-radius: 5em;" +
                "-fx-min-width: 15px; " +
                "-fx-min-height: 15px; " +
                "-fx-max-width: 15px; " +
                "-fx-max-height: 15px;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 1;" +
                "-fx-background-insets: 1;");
    }

    BoundLine getBoundLine() {
        return this.boundLine;
    }

    boolean hasBoundLine() {
        return boundLine != null;
    }

    private void calcCenter() {
        Bounds bounds = parentPane.sceneToLocal(this.localToScene(this.getBoundsInLocal()));
        centerX.set(bounds.getMinX() + bounds.getWidth()  / 2);
        centerY.set(bounds.getMinY() + bounds.getHeight()  / 2);
    }

    void setConnectionId(String id) {
        this.id = id;
    }

    public String getConnectionId() {
        return this.id;
    }

    public int getParentSceneId() {
        return this.parentSceneId;
    }

    boolean getConnectionType() {
        return this.connectionType;
    }

    ReadOnlyDoubleProperty centerXProperty() {
        return this.centerX.getReadOnlyProperty();
    }

    ReadOnlyDoubleProperty centerYProperty() {
        return this.centerY.getReadOnlyProperty();
    }

    public int getOptionNumber() {
        return optionNumber;
    }

    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }
}
