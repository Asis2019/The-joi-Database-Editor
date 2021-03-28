package com.asis.ui.asis_node;

import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Scene;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;

import static com.asis.utilities.AsisUtils.hackTooltipStartTiming;

public class AsisConnectionButton extends Button {

    public static final String DEFAULT_COLOR = "#a8689e";
    public static final String SINGLE_LINE_COLOR = "#63c763ff";
    public static final String BAD_END_COLOR = "#c76363ff";
    public static final String GOOD_END_COLOR = "#6392c7ff";
    public static final String RANDOM_OUT_COLOR = "#c7c763ff";

    private final ReadOnlyDoubleWrapper centerX = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper centerY = new ReadOnlyDoubleWrapper();

    private final ObservableList<BoundLine> boundLines = FXCollections.observableArrayList(new ArrayList<>());

    private final JOIComponent joiComponent;
    private int optionNumber;
    private final EditorWindow editorWindow;

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

    AsisConnectionButton(boolean connectionType, JOIComponent joiComponent, EditorWindow editorWindow) {
        this.connectionType = connectionType;
        this.joiComponent = joiComponent;
        this.editorWindow = editorWindow;

        setStyle(outputConnectorStyle);
        setCursor(Cursor.HAND);

        localToSceneTransformProperty().addListener((observableValue, transform, t1) -> calcCenter());

        boundLines.addListener((ListChangeListener<BoundLine>) change -> {
            calcCenter();
            processConnectionColors();
        });

        if (getJoiComponent() instanceof Scene) {
            Scene scene = (Scene) getJoiComponent();
            scene.goodEndProperty().addListener((observableValue, aBoolean, t1) -> processConnectionColors());
            scene.badEndProperty().addListener((observableValue, aBoolean, t1) -> processConnectionColors());
        }

        processConnectionColors();
    }

    void calculateTooltip() {
        if (!hasBoundLine()) {
            setTooltip(null);
            return;
        }

        StringBuilder tooltipText = new StringBuilder();
        for (BoundLine line : getBoundLines()) {
            if (connectionType) {
                tooltipText.append(line.getStartPointConnectionObject().getJoiComponent().getComponentTitle());
            } else {
                tooltipText.append(line.getEndPointConnectionObject().getJoiComponent().getComponentTitle());
            }
            tooltipText.append("\n");
        }

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        hackTooltipStartTiming(tooltip);
        tooltip.getScene().cursorProperty().bind(cursorProperty());
        setTooltip(tooltip);
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
        return getBoundLines().get(getBoundLines().size() - 1);
    }

    void cycleList() {
        //This method exists so that you can select different lines from the input
        BoundLine line = getBoundLine();

        getBoundLines().remove(line);
        getBoundLines().add(0, line);
    }

    boolean hasBoundLine() {
        return !boundLines.isEmpty();
    }

    private void calcCenter() {
        Bounds bounds = editorWindow.getInfinityPane().getContainer().sceneToLocal(localToScene(getBoundsInLocal()));
        centerX.set(bounds.getMinX() + bounds.getWidth() / 2);
        centerY.set(bounds.getMinY() + bounds.getHeight() / 2);
    }

    void processConnectionColors() {
        if (getConnectionType() && getJoiComponent() instanceof Scene) {
            Scene scene = (Scene) getJoiComponent();

            if (scene.isGoodEnd()) {
                setButtonColor(GOOD_END_COLOR);
                return;
            } else if (scene.isBadEnd()) {
                setButtonColor(BAD_END_COLOR);
                return;
            }
        }

        if (getBoundLines().size() > 1) {
            setButtonColor(RANDOM_OUT_COLOR);
        } else if (getBoundLines().size() == 1) {
            setButtonColor(SINGLE_LINE_COLOR);
        } else {
            setButtonColor(DEFAULT_COLOR);
        }
    }

    public int getParentSceneId() {
        return getJoiComponent().getComponentId();
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

    ObservableList<BoundLine> getBoundLines() {
        return boundLines;
    }

    public JOIComponent getJoiComponent() {
        return joiComponent;
    }
}
