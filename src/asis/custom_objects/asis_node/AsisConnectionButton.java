package asis.custom_objects.asis_node;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

class AsisConnectionButton extends Button {
    private ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
    private ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();

    private Pane parentPane;
    private String id = null;
    private String parentSceneId;
    private BoundLine boundLine;
    private boolean connectionType; //false is output true is input

    private static final String outputConnectorStyle =
            "-fx-background-color: rgba(99, 199, 99, 1), transparent, transparent;" +
            "-fx-border-radius: 5em;" +
            "-fx-min-width: 15px; " +
            "-fx-min-height: 15px; " +
            "-fx-max-width: 15px; " +
            "-fx-max-height: 15px;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 1;" +
            "-fx-background-insets: 1;";

    AsisConnectionButton(Pane parentPane, boolean connectionType, String parentSceneId) {
        this.parentPane = parentPane;
        this.connectionType = connectionType;
        this.parentSceneId = parentSceneId;

        setStyle(outputConnectorStyle);
        setCursor(Cursor.HAND);

        calcCenter();

        this.localToSceneTransformProperty().addListener((observableValue, transform, t1) -> calcCenter());
    }

    void setBoundLine(BoundLine boundLine) {
        this.boundLine = boundLine;
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

    String getConnectionId() {
        return this.id;
    }

    String getParentSceneId() {
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
}
