package com.asis.ui.asis_node;

import com.asis.Story;
import com.asis.controllers.Controller;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.json.JSONArray;
import org.json.JSONObject;

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

    ScrollPane getScrollPane() {
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

        lineList.add(boundLine);

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
                lineList.remove(boundLine);
                inputConnection.setBoundLine(null);
            }

            if (currentOutputConnection != null) {
                if (currentOutputConnection.hasBoundLine()) {
                    //Check if any lines have the same output and input already
                    for (BoundLine line : lineList) {
                        if (line.getStartPointConnectionObject() == currentOutputConnection && line.getEndPointConnectionObject() == inputConnection) {
                            System.out.println("Duplicate line found");
                            currentOutputConnection.getBoundLine().endXProperty().unbind();
                            currentOutputConnection.getBoundLine().endYProperty().unbind();
                            root.getChildren().remove(currentOutputConnection.getBoundLine());
                            lineList.remove(line);
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
        if(outputConnection.getConnectionId().contains("dialog_option")) {
            //Remove from inner dialog location
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                lineList.remove(boundLine);
                Story.getInstance().removeValueFromDialogOptionGotoRange(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), inputConnection.getParentSceneId());
                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    Controller.getInstance().setNewChanges();
                    return;
                }

                //Convert to old method
                outputConnection.setButtonColor("#63c763ff");
                Story.getInstance().convertValueFromDialogOptionGotoRangeToSingle(outputConnection.getParentSceneId(), outputConnection.getOptionNumber());
            } else {
                lineList.remove(boundLine);
                Story.getInstance().removeDialogOptionData(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), "gotoScene");
                outputConnection.setBoundLine(null);
            }
        } else {
            //Remove from upper scene location
            if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                lineList.remove(boundLine);
                Story.getInstance().removeValueFromSceneGotoRange(outputConnection.getParentSceneId(), inputConnection.getParentSceneId());
                outputConnection.setBoundLine(null);

                //Check if after removing there are still multiple lines
                if (getTotalLinesConnectedToOutput(outputConnection) > 1) {
                    Controller.getInstance().setNewChanges();
                    return;
                }

                //Convert to old method
                outputConnection.setButtonColor("#63c763ff");
                JSONArray storyData = Story.getInstance().getStoryDataJson().getJSONArray("JOI");
                int amountOfScenes = Story.getInstance().getStoryDataJson().getJSONArray("JOI").length();
                for (int i = 0; i < amountOfScenes; i++) {
                    if (storyData.getJSONObject(i).has("gotoSceneInRange")) {
                        int gotoValue = storyData.getJSONObject(i).getJSONArray("gotoSceneInRange").getInt(0);
                        Story.getInstance().addDataToScene(outputConnection.getParentSceneId(), "gotoScene", gotoValue);
                        Story.getInstance().removeDataFromScene(outputConnection.getParentSceneId(), "gotoSceneInRange");
                        break;
                    }
                }
            } else {
                lineList.remove(boundLine);
                outputConnection.setBoundLine(null);
                Story.getInstance().removeDataFromScene(outputConnection.getParentSceneId(), "gotoScene");
            }
        }
        Controller.getInstance().setNewChanges();
    }

    private void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        //Process where to add the jump to
        if(outputConnection.getConnectionId().contains("dialog_option")) {
            if(getTotalLinesConnectedToOutput(outputConnection) > 1) {
                outputConnection.setButtonColor("#c7c763");
                Story.getInstance().addValueToDialogOptionGotoRange(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), inputConnection.getParentSceneId());

                //Add old value if present
                JSONObject dialogData = Story.getInstance().getDialogData(outputConnection.getParentSceneId()).getJSONArray("option"+outputConnection.getOptionNumber()).getJSONObject(0);
                if(dialogData.has("gotoScene")) {
                    int gotoValue = dialogData.getInt("gotoScene");
                    Story.getInstance().addValueToDialogOptionGotoRange(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), gotoValue);
                    Story.getInstance().removeDialogOptionData(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), "gotoScene");
                }
            } else {
                Story.getInstance().addDialogOptionData(outputConnection.getParentSceneId(), outputConnection.getOptionNumber(), "gotoScene", inputConnection.getParentSceneId());
            }
        } else {
            if(getTotalLinesConnectedToOutput(outputConnection) > 1) {
                outputConnection.setButtonColor("#c7c763");
                Story.getInstance().addValueToSceneGotoRange(outputConnection.getParentSceneId(), inputConnection.getParentSceneId());

                //Add old value if present
                JSONArray storyData = Story.getInstance().getStoryDataJson().getJSONArray("JOI");
                int amountOfScenes = Story.getInstance().getStoryDataJson().getJSONArray("JOI").length();
                for(int i=0; i < amountOfScenes; i++) {
                    if (storyData.getJSONObject(i).has("gotoScene")) {
                        int gotoValue = storyData.getJSONObject(i).getInt("gotoScene");
                        Story.getInstance().addValueToSceneGotoRange(outputConnection.getParentSceneId(), gotoValue);
                        Story.getInstance().removeDataFromScene(outputConnection.getParentSceneId(), "gotoScene");
                        break;
                    }
                }
            } else {
                Story.getInstance().addDataToScene(outputConnection.getParentSceneId(), "gotoScene", inputConnection.getParentSceneId());
            }
        }
        Controller.getInstance().setNewChanges();
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
            for (BoundLine line : lineList) {
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
        ArrayList<BoundLine> consistentDataList = new ArrayList<>(lineList);
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
}
