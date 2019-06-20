package asis.custom_objects.asis_node;

import asis.Controller;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneNodeMainController {
    private Pane root = new Pane();
    private ScrollPane scrollPane;

    private double menuBarOffset;

    private List<AsisConnectionButton> inputConnections = new ArrayList<>();

    private AsisConnectionButton currentOutputConnection;
    private Controller controller;

    private DoubleProperty mouseX = new SimpleDoubleProperty();
    private DoubleProperty mouseY = new SimpleDoubleProperty();

    private boolean dragActive = false;

    public SceneNodeMainController(Controller controller) {
        this.controller = controller;
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public void setMenuBarOffset(double height) {
        this.menuBarOffset = height;
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

    void addInputConnection(AsisConnectionButton asisConnectionButton) {
        inputConnections.add(asisConnectionButton);
    }

    public void createConnection(AsisConnectionButton from, AsisConnectionButton to) {
        BoundLine boundLine = new BoundLine(from.centerXProperty(), from.centerYProperty());
        from.setBoundLine(boundLine);
        boundLine.setStartPointConnectionObject(from);

        boundLine.controlX1Property().bind(Bindings.add(boundLine.startXProperty(), 100));
        boundLine.controlX2Property().bind(Bindings.add(boundLine.endXProperty(), -100));
        boundLine.controlY1Property().bind(Bindings.add(boundLine.startYProperty(), 0));
        boundLine.controlY2Property().bind(Bindings.add(boundLine.endYProperty(), 0));

        boundLine.setUserData(from);
        boundLine.endXProperty().bind(to.centerXProperty());
        boundLine.endYProperty().bind(to.centerYProperty());
        boundLine.setEndPointConnectionObject(to);

        root.getChildren().add(0, boundLine);
    }

    private void startDrag() {
        if (dragActive)
            return;

        dragActive = true;

        if(currentOutputConnection.hasBoundLine()) {
            root.getChildren().remove(currentOutputConnection.getBoundLine());
        }

        BoundLine boundLine = new BoundLine(currentOutputConnection.centerXProperty(), currentOutputConnection.centerYProperty());
        currentOutputConnection.setBoundLine(boundLine);
        boundLine.setStartPointConnectionObject(currentOutputConnection);

        boundLine.controlX1Property().bind(Bindings.add(boundLine.startXProperty(), 100));
        boundLine.controlX2Property().bind(Bindings.add(boundLine.endXProperty(), -100));
        boundLine.controlY1Property().bind(Bindings.add(boundLine.startYProperty(), 0));
        boundLine.controlY2Property().bind(Bindings.add(boundLine.endYProperty(), 0));

        boundLine.setUserData(currentOutputConnection);
        boundLine.endXProperty().bind(mouseX);
        boundLine.endYProperty().bind(mouseY);

        root.getChildren().add(0, boundLine);
    }

    private void stopDrag(AsisConnectionButton inputConnection) {
        if(!inputConnection.getConnectionType()) {
            BoundLine boundLine = inputConnection.getBoundLine();
            boundLine.endXProperty().unbind();
            boundLine.endYProperty().unbind();
            root.getChildren().remove(boundLine);
            inputConnection.setBoundLine(null);

            controller.removeConnectionFromStory(inputConnection.getParentSceneId());
        }

        /*if(currentOutputConnection.getParentSceneId().equals(inputConnection.getParentSceneId())) {
            System.out.println("Found same scene");
            BoundLine boundLine = currentOutputConnection.getBoundLine();
            boundLine.endXProperty().unbind();
            boundLine.endYProperty().unbind();
            root.getChildren().remove(boundLine);
            currentOutputConnection.setBoundLine(null);
            currentOutputConnection = null;
        }*/

        if(currentOutputConnection != null) {
            if (currentOutputConnection.hasBoundLine()) {
                //Connection established
                BoundLine boundLine = currentOutputConnection.getBoundLine();
                boundLine.endXProperty().unbind();
                boundLine.endYProperty().unbind();
                boundLine.endXProperty().bind(inputConnection.centerXProperty());
                boundLine.endYProperty().bind(inputConnection.centerYProperty());
                inputConnection.setBoundLine(boundLine);

                boundLine.setEndPointConnectionObject(inputConnection);

                controller.addConnectionToStory(currentOutputConnection, inputConnection);
            }
        }

        dragActive = false;
        currentOutputConnection = null;
    }

    void mouseMoved(MouseEvent mouseEvent) {
        Bounds bounds = scrollPane.getViewportBounds();
        double lowestXPixelShown = -1 * bounds.getMinX();
        double lowestYPixelShown = -1 * bounds.getMinY() - menuBarOffset;

        mouseX.set(lowestXPixelShown + mouseEvent.getSceneX());
        mouseY.set(lowestYPixelShown + mouseEvent.getSceneY());
    }

    void mousePressed(AsisConnectionButton asisConnectionButton) {
        currentOutputConnection = asisConnectionButton;
        startDrag();
    }

    void mouseReleased(MouseEvent mouseEvent) {
        Optional<AsisConnectionButton> asisConnectionButton = findNode(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        if (asisConnectionButton.isPresent()) {
            stopDrag(asisConnectionButton.get());
        } else {
            stopDrag(currentOutputConnection);
        }
    }

    public void notifySceneRemoved(SceneNode sceneNode) {
        //Check if connection is present for outputs
        for (AsisConnectionButton connection : sceneNode.getOutputButtons()) {
            if(connection.hasBoundLine()) {
                root.getChildren().remove(connection.getBoundLine());
                connection.setBoundLine(null);

                controller.removeConnectionFromStory(connection.getParentSceneId());
            }
        }

        //Check if input has any connection
        if(sceneNode.getInputConnection().hasBoundLine()) {
            root.getChildren().remove(sceneNode.getInputConnection().getBoundLine());

            controller.removeConnectionFromStory(sceneNode.getInputConnection().getBoundLine().getStartPointConnectionObject().getParentSceneId());

            sceneNode.getInputConnection().setBoundLine(null);
        }
    }
}
