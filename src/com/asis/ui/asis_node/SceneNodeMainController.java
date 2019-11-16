package com.asis.ui.asis_node;

import com.asis.joi.JOIPackage;
import com.asis.joi.components.GotoScene;
import com.asis.joi.components.Scene;
import com.asis.joi.components.dialog.DialogOption;
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
    private List<BoundLine> lineList = new ArrayList<>();

    private AsisConnectionButton currentOutputConnection;

    private DoubleProperty mouseX = new SimpleDoubleProperty();
    private DoubleProperty mouseY = new SimpleDoubleProperty();

    private JOIPackage joiPackage;

    private boolean dragActive = false;

    public SceneNodeMainController(JOIPackage joiPackage) {
        setJoiPackage(joiPackage);
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
        to.setBoundLine(boundLine);
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

        getLineList().add(boundLine);

        if (getTotalLinesConnectedToOutput(from) > 1) {
            from.setButtonColor("#c7c763");
        }
    }

    private int getTotalLinesConnectedToOutput(AsisConnectionButton asisConnectionButton) {
        int lineConnectionsToOutput = 0;
        for (BoundLine line : lineList) {
            if(line.getStartPointConnectionObject() == asisConnectionButton) {
                lineConnectionsToOutput++;
            }
        }
        return lineConnectionsToOutput;
    }

    private void startDrag() {
        if (dragActive)
            return;

        dragActive = true;

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

        lineList.add(boundLine);

        root.getChildren().add(0, boundLine);
    }

    private void stopDrag(AsisConnectionButton inputConnection) {
        if(inputConnection != null) {
            //Destroy line if input connection is somehow an output type
            if (!inputConnection.getConnectionType()) {
                BoundLine boundLine = inputConnection.getBoundLine();
                boundLine.endXProperty().unbind();
                boundLine.endYProperty().unbind();
                root.getChildren().remove(boundLine);
                getLineList().remove(boundLine);
                inputConnection.setBoundLine(null);
            }

            if (currentOutputConnection != null) {
                if (currentOutputConnection.hasBoundLine()) {
                    //Check if any lines have the same output and input already
                    for (BoundLine line : getLineList()) {
                        if (line.getStartPointConnectionObject() == currentOutputConnection && line.getEndPointConnectionObject() == inputConnection) {
                            System.out.println("Duplicate line found");
                            currentOutputConnection.getBoundLine().endXProperty().unbind();
                            currentOutputConnection.getBoundLine().endYProperty().unbind();
                            root.getChildren().remove(currentOutputConnection.getBoundLine());
                            getLineList().remove(line);
                            currentOutputConnection.setBoundLine(null);
                            dragActive = false;
                            currentOutputConnection = null;
                            return;
                        }
                    }

                    //Connection properly established
                    BoundLine boundLine = currentOutputConnection.getBoundLine();
                    boundLine.endXProperty().unbind();
                    boundLine.endYProperty().unbind();
                    boundLine.endXProperty().bind(inputConnection.centerXProperty());
                    boundLine.endYProperty().bind(inputConnection.centerYProperty());
                    inputConnection.setBoundLine(boundLine);
                    boundLine.setEndPointConnectionObject(inputConnection);

                    addConnectionToStory(currentOutputConnection, inputConnection);
                }
            }

            dragActive = false;
            currentOutputConnection = null;
        }
    }

    private void removeConnectionFromStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection, BoundLine boundLine) {
        final Scene scene = getJoiPackage().getJoi().getScene(outputConnection.getParentSceneId());

        if(outputConnection.getConnectionId().contains("dialog_option")) {
            //Remove from inner dialog location
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                getLineList().remove(boundLine);
                scene.getDialog().getOptionArrayList().get(outputConnection.getOptionNumber()).getGotoScene().removeValue(outputConnection.getOptionNumber());
                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    return;
                }

                //Convert to old method
                outputConnection.setButtonColor("#63c763ff");
            } else {
                getLineList().remove(boundLine);
                scene.getDialog().getOptionArrayList().get(outputConnection.getOptionNumber()).setGotoScene(null);
                outputConnection.setBoundLine(null);
            }
        } else {
            //Remove from upper scene location
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                getLineList().remove(boundLine);
                scene.getGotoScene().removeValue(inputConnection.getParentSceneId());
                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    return;
                }

                outputConnection.setButtonColor("#63c763ff");
            } else {
                getLineList().remove(boundLine);
                outputConnection.setBoundLine(null);
                scene.setGotoScene(null);
            }
        }
    }

    private void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        final Scene scene = getJoiPackage().getJoi().getScene(outputConnection.getParentSceneId());

        //Process where to add the jump to
        if(outputConnection.getConnectionId().contains("dialog_option")) {
            final DialogOption dialogOption = scene.getDialog().getOptionArrayList().get(outputConnection.getOptionNumber());

            if(getTotalLinesConnectedToOutput(outputConnection) > 1) {
                outputConnection.setButtonColor("#c7c763");
                dialogOption.getGotoScene().addValue(inputConnection.getParentSceneId());
            } else {
                dialogOption.setGotoScene(new GotoScene());
                dialogOption.getGotoScene().addValue(inputConnection.getParentSceneId());
            }
        } else {
            if(getTotalLinesConnectedToOutput(outputConnection) > 1) {
                outputConnection.setButtonColor("#c7c763");
                scene.getGotoScene().addValue(inputConnection.getParentSceneId());
            } else {
                scene.setGotoScene(new GotoScene());
                scene.getGotoScene().addValue(inputConnection.getParentSceneId());
            }
        }
    }

    void mouseMoved(MouseEvent mouseEvent) {
        Bounds bounds = scrollPane.getViewportBounds();
        double lowestXPixelShown = -1 * bounds.getMinX();
        double lowestYPixelShown = -1 * bounds.getMinY() - menuBarOffset;

        mouseX.set(lowestXPixelShown + mouseEvent.getSceneX());
        mouseY.set(lowestYPixelShown + mouseEvent.getSceneY());
    }

    void mousePressed(AsisConnectionButton asisConnectionButton) {
        if(!asisConnectionButton.getConnectionType()) {
            //Connection is an output type
            currentOutputConnection = asisConnectionButton;
            startDrag();
        } else {
            //Connection is an input type
            for (BoundLine line : getLineList()) {
                if (line.getEndPointConnectionObject() == asisConnectionButton) {
                    currentOutputConnection = line.getStartPointConnectionObject();
                    root.getChildren().remove(line);
                    removeConnectionFromStory(currentOutputConnection, asisConnectionButton, line);
                    startDrag();
                    break;
                }
            }
        }
    }

    void mouseReleased(MouseEvent mouseEvent) {
        //Check if an input node is present under mouse when released
        Optional<AsisConnectionButton> asisConnectionButton = findNode(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        if (asisConnectionButton.isPresent()) {
            stopDrag(asisConnectionButton.get());
        } else {
            stopDrag(currentOutputConnection);
        }
    }

    public void notifySceneRemoved(SceneNode sceneNode) {
        //Check if connection is present for outputs
        ArrayList<BoundLine> consistentDataList = new ArrayList<>(getLineList());
        for (AsisConnectionButton connection : sceneNode.getOutputButtons()) {
            for (BoundLine line : consistentDataList) {
                if (connection == line.getStartPointConnectionObject()) {
                    removeConnectionFromStory(connection, line.getEndPointConnectionObject(), line);
                    root.getChildren().remove(line);
                }
            }
        }

        //Check if input has any connection
        for (BoundLine line : consistentDataList) {
            if(sceneNode.getInputConnection() == line.getEndPointConnectionObject()) {
                removeConnectionFromStory(line.getStartPointConnectionObject(), sceneNode.getInputConnection(), line);
                root.getChildren().remove(line);
            }
        }
    }

    public JOIPackage getJoiPackage() {
        return joiPackage;
    }
    public void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }

    public List<BoundLine> getLineList() {
        return lineList;
    }
    public void setLineList(List<BoundLine> lineList) {
        this.lineList = lineList;
    }

    public void setScrollPane(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }
    public ScrollPane getScrollPane() {
        return this.scrollPane;
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
}
