package com.asis.ui.asis_node;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class AsisConnectionButton extends Button {

    public static final String DEFAULT_COLOR = "#63c763ff";
    public static final String BAD_END_COLOR = "#c76363ff";
    public static final String GOOD_END_COLOR = "#6392c7ff";
    public static final String RANDOM_OUT_COLOR = "#c7c763ff";

    private final ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();

    private final Pane parentPane;
    private BoundLine boundLine;

    private final int parentSceneId;
    private int optionNumber;

    private final boolean connectionType; //false is output true is input

    private static final String outputConnectorStyle =
            "-fx-background-color: " + DEFAULT_COLOR + ", transparent, transparent;" +
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

        localToSceneTransformProperty().addListener((observableValue, transform, t1) -> calcCenter());
    }

    void setBoundLine(BoundLine boundLine) {
        this.boundLine = boundLine;
    }

    void setButtonColor(String hexColor) {
        setStyle("-fx-background-color: " + hexColor + ", transparent, transparent;" +
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

    void calcCenter() {
        Bounds bounds = parentPane.sceneToLocal(localToScene(getBoundsInLocal()));
        centerX.set(bounds.getMinX() + bounds.getWidth() / 2);
        centerY.set(bounds.getMinY() + bounds.getHeight() / 2);
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
