package asis.custom_objects.asis_node;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneNodeMainController {
    private Pane root = new Pane();

    private List<AsisConnectionButton> inputConnections = new ArrayList<>();
    private List<BoundLine> boundLines = new ArrayList<>();

    private int index = 0;

    private DoubleProperty mouseX = new SimpleDoubleProperty();
    private DoubleProperty mouseY = new SimpleDoubleProperty();

    private boolean dragActive = false;

    public SceneNodeMainController(Scene scene) {

    }

    public void setPane(Pane pane) {
        root = pane;
    }

    public Pane getPane() {
        return this.root;
    }

    private Optional<AsisConnectionButton> findNode(double x, double y) {
        for (AsisConnectionButton n : inputConnections) {
            Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());

            if(x >= boundsInScene.getMinX() && x <= boundsInScene.getMaxX() && y >= boundsInScene.getMinY() && y <= boundsInScene.getMaxY()) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    public void addInputConnection(AsisConnectionButton asisConnectionButton) {
        inputConnections.add(asisConnectionButton);
    }

    private void startDrag(AsisConnectionButton asisConnectionButton) {
        if (dragActive)
            return;

        dragActive = true;

        boundLines.add(index, new BoundLine(asisConnectionButton.centerXProperty(), asisConnectionButton.centerYProperty()));
        boundLines.get(index).controlX1Property().bind(
                Bindings.add(boundLines.get(index).startXProperty(), 100)
        );

        boundLines.get(index).controlX2Property().bind(
                Bindings.add(boundLines.get(index).endXProperty(), -100)
        );

        boundLines.get(index).controlY1Property().bind(
                Bindings.add(boundLines.get(index).startYProperty(), 0)
        );

        boundLines.get(index).controlY2Property().bind(
                Bindings.add(boundLines.get(index).endYProperty(), 0)
        );

        boundLines.get(index).setUserData(asisConnectionButton);
        boundLines.get(index).endXProperty().bind(mouseX);
        boundLines.get(index).endYProperty().bind(mouseY);

        root.getChildren().add(0, boundLines.get(index));
    }

    private void stopDrag(AsisConnectionButton asisConnectionButton) {
        dragActive = false;

        if(asisConnectionButton != null) {
            // distinct node
            boundLines.get(index).endXProperty().unbind();
            boundLines.get(index).endYProperty().unbind();
            boundLines.get(index).endXProperty().bind(asisConnectionButton.centerXProperty());
            boundLines.get(index).endYProperty().bind(asisConnectionButton.centerYProperty());
        } else {
            // same node
            stopDrag();
        }
    }

    private void stopDrag() {
        if(dragActive) {
            boundLines.get(index).endXProperty().unbind();
            boundLines.get(index).endYProperty().unbind();
            root.getChildren().remove(boundLines.get(index));
        }

        dragActive = false;
    }

    void mouseMoved(MouseEvent mouseEvent) {
        mouseX.set(mouseEvent.getSceneX());
        mouseY.set(mouseEvent.getSceneY());
    }

    void mousePressed(AsisConnectionButton asisConnectionButton, MouseEvent mouseEvent) {
        startDrag(asisConnectionButton);
    }

    void mouseReleased(MouseEvent mouseEvent) {
        Optional<AsisConnectionButton> asisConnectionButton = findNode(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        if (asisConnectionButton.isPresent()) {
            stopDrag(asisConnectionButton.get());
        } else {
            stopDrag();
        }
    }
}
