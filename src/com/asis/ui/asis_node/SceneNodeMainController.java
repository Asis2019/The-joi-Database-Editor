package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
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
        BoundLine boundLine = new BoundLine(from, to);
        from.setBoundLine(boundLine);
        to.setBoundLine(boundLine);

        root.getChildren().add(0, boundLine);

        getLineList().add(boundLine);

        if (getTotalLinesConnectedToOutput(from) > 1)
            from.setButtonColor(AsisConnectionButton.RANDOM_OUT_COLOR);
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
        currentOutputConnection.calcCenter();

        BoundLine connectionLine = new BoundLine(currentOutputConnection);
        currentOutputConnection.setBoundLine(connectionLine);

        //Set the line's end to follow the mouse properties
        connectionLine.endXProperty().bind(mouseX);
        connectionLine.endYProperty().bind(mouseY);

        getLineList().add(connectionLine);
        root.getChildren().add(0, connectionLine);
    }

    private void stopDrag(AsisConnectionButton inputConnection) {
        if (currentOutputConnection != null && currentOutputConnection.hasBoundLine()) {
            //Check if any lines have the same output and input already
            for (BoundLine line : getLineList()) {
                if (line.getStartPointConnectionObject() == currentOutputConnection && line.getEndPointConnectionObject() == inputConnection) {
                    currentOutputConnection.getBoundLine().unbindEnd();

                    root.getChildren().remove(currentOutputConnection.getBoundLine());
                    getLineList().remove(currentOutputConnection.getBoundLine());
                    currentOutputConnection.setBoundLine(null);
                    dragActive = false;
                    currentOutputConnection = null;
                    return;
                }
            }

            //Connection properly established
            inputConnection.calcCenter();
            BoundLine boundLine = currentOutputConnection.getBoundLine();
            boundLine.unbindEnd();

            boundLine.setEndPointConnectionObject(inputConnection);
            inputConnection.setBoundLine(boundLine);

            addConnectionToStory(currentOutputConnection, inputConnection);
        }

        dragActive = false;
        currentOutputConnection = null;
    }

    private void removeConnectionFromStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection, BoundLine boundLine) {
        final JOIComponent joiComponent = getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());

        if(outputConnection.getId().contains("dialog_option")) {
            //Remove from inner dialog location
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                getLineList().remove(boundLine);
                ((Scene) joiComponent).getComponent(Dialog.class).getOptionArrayList().get(outputConnection.getOptionNumber()).getGotoScene().removeValue(outputConnection.getOptionNumber());
                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    return;
                }

                //Convert to old method
                outputConnection.setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            } else {
                getLineList().remove(boundLine);
                ((Scene) joiComponent).getComponent(Dialog.class).getOptionArrayList().get(outputConnection.getOptionNumber()).setGotoScene(null);
                outputConnection.setBoundLine(null);
            }
        } else {
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                getLineList().remove(boundLine);

                GotoScene gotoScene;
                if(joiComponent instanceof Scene) {
                    gotoScene = ((Scene) joiComponent).getComponent(GotoScene.class);
                } else if(joiComponent instanceof VariableSetter) {
                    gotoScene = ((VariableSetter) joiComponent).getGotoScene();
                } else if(joiComponent instanceof Condition) {
                    if(outputConnection.getId().equals("true_output")) {
                        gotoScene = ((Condition) joiComponent).getGotoSceneTrue();
                    } else if(outputConnection.getId().equals("false_output")) {
                        gotoScene = ((Condition) joiComponent).getGotoSceneFalse();
                    } else {
                        throw new RuntimeException("connection id was not true or false!");
                    }
                } else {
                    return;
                }
                gotoScene.removeValue(inputConnection.getParentSceneId());

                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    return;
                }

                outputConnection.setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            } else {
                getLineList().remove(boundLine);
                outputConnection.setBoundLine(null);

                if(joiComponent instanceof Scene) {
                    ((Scene) joiComponent).removeComponent(GotoScene.class);
                } else if(joiComponent instanceof VariableSetter) {
                    ((VariableSetter) joiComponent).setGotoScene(null);
                } else if(joiComponent instanceof Condition) {
                    if(outputConnection.getId().equals("true_output")) {
                        ((Condition) joiComponent).setGotoSceneTrue(null);
                    } else if(outputConnection.getId().equals("false_output")) {
                        ((Condition) joiComponent).setGotoSceneFalse(null);
                    }
                }
            }
        }
    }

    private void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        final JOIComponent component = getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());

        //Process where to add the jump to
        if(outputConnection.getId().contains("dialog_option")) {
            final DialogOption dialogOption = ((Scene) component).getComponent(Dialog.class).getOptionArrayList().get(outputConnection.getOptionNumber());

            if(getTotalLinesConnectedToOutput(outputConnection) > 1)
                outputConnection.setButtonColor(AsisConnectionButton.RANDOM_OUT_COLOR);
            else
                dialogOption.setGotoScene(new GotoScene());

            dialogOption.getGotoScene().addValue(inputConnection.getParentSceneId());
        } else {
            if(getTotalLinesConnectedToOutput(outputConnection) > 1)
                outputConnection.setButtonColor(AsisConnectionButton.RANDOM_OUT_COLOR);
            else {
                if(component instanceof Scene) {
                    ((Scene) component).addComponent(new GotoScene());
                    ((Scene) component).getComponent(GotoScene.class).addValue(inputConnection.getParentSceneId());
                } else if(component instanceof VariableSetter) {
                    ((VariableSetter) component).setGotoScene(new GotoScene());
                    ((VariableSetter) component).getGotoScene().addValue(inputConnection.getParentSceneId());
                } else if(component instanceof Condition) {
                    if(outputConnection.getId().equals("true_output")) {
                        ((Condition) component).setGotoSceneTrue(new GotoScene());
                        ((Condition) component).getGotoSceneTrue().addValue(inputConnection.getParentSceneId());
                    } else if(outputConnection.getId().equals("false_output")) {
                        ((Condition) component).setGotoSceneFalse(new GotoScene());
                        ((Condition) component).getGotoSceneFalse().addValue(inputConnection.getParentSceneId());
                    }
                }
            }

        }
    }

    void mouseMoved(MouseEvent mouseEvent) {
        Controller controller = Controller.getInstance();
        final double menuBarOffset = controller.mainMenuBar.getHeight() + controller.toolBar.getHeight();

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
            BoundLine boundLine = currentOutputConnection.getBoundLine();
            boundLine.unbindEnd();

            root.getChildren().remove(boundLine);
            getLineList().remove(boundLine);
            currentOutputConnection.setBoundLine(null);

            dragActive = false;
            currentOutputConnection = null;
        }
    }

    public void notifyComponentNodeRemoved(JOIComponentNode joiComponentNode) {
        //Check if connection is present for outputs
        ArrayList<BoundLine> consistentDataList = new ArrayList<>(getLineList());
        for (AsisConnectionButton connection : joiComponentNode.getOutputButtons()) {
            for (BoundLine line : consistentDataList) {
                if (connection == line.getStartPointConnectionObject()) {
                    removeConnectionFromStory(connection, line.getEndPointConnectionObject(), line);
                    root.getChildren().remove(line);
                }
            }
        }

        //Check if input has any connection
        for (BoundLine line : consistentDataList) {
            if(joiComponentNode.getInputConnection() == line.getEndPointConnectionObject()) {
                removeConnectionFromStory(line.getStartPointConnectionObject(), joiComponentNode.getInputConnection(), line);
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

    public void setPane(Pane pane) {
        root = pane;
    }
    public Pane getPane() {
        return this.root;
    }
}
