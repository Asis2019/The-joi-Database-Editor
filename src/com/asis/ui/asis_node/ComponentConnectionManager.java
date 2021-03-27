package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.joi.LoadJOIService;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ComponentConnectionManager {

    private static ComponentConnectionManager componentConnectionManager = new ComponentConnectionManager();

    private final Controller controller = Controller.getInstance();
    private final LoadJOIService loadJOIService = LoadJOIService.getInstance();

    private AsisConnectionButton currentlyActiveConnection = null;

    private final DoubleProperty mouseX = new SimpleDoubleProperty();
    private final DoubleProperty mouseY = new SimpleDoubleProperty();

    private ComponentConnectionManager() {
        if (componentConnectionManager == null) componentConnectionManager = this;
    }

    public static ComponentConnectionManager getInstance() {
        return componentConnectionManager;
    }

    /**
     * Will remove all lines connected to the node passed
     *
     * @param joiComponentNode - the node that was deleted
     */
    public void removeConnection(JOIComponentNode joiComponentNode) {
        //Remove inputs

        // New list is needed to prevent ConcurrentModificationException
        ArrayList<BoundLine> boundLineArrayListInput = new ArrayList<>(joiComponentNode.getInputConnection().getBoundLines());
        for (BoundLine line : boundLineArrayListInput) {
            removeLine(line);
        }

        //Remove outputs
        for (AsisConnectionButton asisConnectionButton : joiComponentNode.getOutputButtons()) {
            // New list is needed to prevent ConcurrentModificationException
            ArrayList<BoundLine> boundLineArrayList = new ArrayList<>(asisConnectionButton.getBoundLines());
            for (BoundLine line : boundLineArrayList) removeLine(line);
        }
    }

    /**
     * Create a bound line from the start point to the end point connections.
     *
     * @param from - the connection start point
     * @param to   - the connection end point
     */
    public void createConnection(AsisConnectionButton from, AsisConnectionButton to) {
        BoundLine boundLine = new BoundLine(from, to);
        from.getBoundLines().add(boundLine);
        to.getBoundLines().add(boundLine);

        controller.getInfinityPane().getContainer().getChildren().add(0, boundLine);

        if (from.getBoundLines().size() > 1)
            from.setButtonColor(AsisConnectionButton.RANDOM_OUT_COLOR);

        from.calculateTooltip();
        to.calculateTooltip();
    }

    /**
     * Check if an input or output was pressed. If output, create new line. If input get line and unbind it.
     *
     * @param asisConnectionButton - the connection pressed
     */
    void mousePressed(AsisConnectionButton asisConnectionButton) {
        currentlyActiveConnection = asisConnectionButton;

        if (!asisConnectionButton.getConnectionType()) {
            //Connection is an output type
            createNewBoundLine();
        } else if(asisConnectionButton.hasBoundLine()) {
            //Connection is an input type
            BoundLine line = asisConnectionButton.getBoundLine();
            line.unbindEnd();

            line.endXProperty().bind(mouseX);
            line.endYProperty().bind(mouseY);
        }
    }

    /**
     * Update the mouse position.
     *
     * @param mouseEvent a mouse event
     */
    void mouseMoved(MouseEvent mouseEvent) {
        final double menuBarOffset = controller.mainMenuBar.getHeight() + controller.toolBar.getHeight();

        Point2D placementCoordinates = controller.getInfinityPane().sceneToWorld(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        mouseX.set(placementCoordinates.getX());
        mouseY.set(placementCoordinates.getY() - menuBarOffset);
    }

    /**
     * Check if mouse was released on top of a connection point. If yes, complete connection. If not, cancel connection.
     *
     * @param mouseEvent a mouseEvent
     */
    void mouseReleased(MouseEvent mouseEvent) {
        // Check if an input node is present under mouse when released
        PickResult pickResult = mouseEvent.getPickResult();

        // Check if mouse is over a connection button
        if (pickResult.getIntersectedNode() instanceof AsisConnectionButton) {
            AsisConnectionButton asisConnectionButton = (AsisConnectionButton) pickResult.getIntersectedNode();

            // Check if connection is an input
            if (asisConnectionButton.getConnectionType()) {
                BoundLine boundLine = currentlyActiveConnection.getBoundLine();

                //Check if any lines have the same output and input already and if connection is not the same as currently active
                if (asisConnectionButton.getBoundLines().contains(boundLine)) {
                    //Line is exact same | was disconnected and reconnected
                    boundLine.setEndPointConnectionObject(asisConnectionButton);
                    asisConnectionButton.cycleList();
                    currentlyActiveConnection = null;
                    return;
                }

                if (asisConnectionButton.getBoundLines().size() >= 1) {
                    for (BoundLine line : asisConnectionButton.getBoundLines()) {
                        if (line.getStartPointConnectionObject() == currentlyActiveConnection && line.getEndPointConnectionObject() == asisConnectionButton) {
                            cancelLineOperation();
                            return;
                        }
                    }
                }

                //*note* currentlyActiveConnection can be an input, which is why some of the code might seem odd

                // Establish connection properly
                AsisConnectionButton from = boundLine.getStartPointConnectionObject();
                removeLine(boundLine);

                //Set tooltip texts
                currentlyActiveConnection.calculateTooltip();
                from.calculateTooltip();
                asisConnectionButton.calculateTooltip();

                createConnection(from, asisConnectionButton);
                addConnectionToStory(from, asisConnectionButton);

                currentlyActiveConnection = null;
                return;
            }
        }

        // Mouse was released above invalid connection area
         cancelLineOperation();
    }

    private void cancelLineOperation() {
        if(currentlyActiveConnection.hasBoundLine()) {
            BoundLine boundLine = currentlyActiveConnection.getBoundLine();
            removeLine(boundLine);
        }

        currentlyActiveConnection = null;
    }

    private void createNewBoundLine() {
        BoundLine connectionLine = new BoundLine(currentlyActiveConnection);
        currentlyActiveConnection.getBoundLines().add(connectionLine);

        //Set the line's end to follow the mouse properties
        connectionLine.endXProperty().bind(mouseX);
        connectionLine.endYProperty().bind(mouseY);

        controller.getInfinityPane().getContainer().getChildren().add(0, connectionLine);
    }

    /**
     * Removes the passed line from the node hierarchy and performs cleanup operations.
     *
     * @param boundLine - the line to remove
     */
    private void removeLine(BoundLine boundLine) {
        controller.getInfinityPane().getContainer().getChildren().remove(boundLine);

        if (boundLine.getEndPointConnectionObject() != null) {
            boundLine.getEndPointConnectionObject().getBoundLines().remove(boundLine);
            boundLine.getEndPointConnectionObject().calculateTooltip();

            removeConnectionFromStory(boundLine.getStartPointConnectionObject(), boundLine.getEndPointConnectionObject().getParentSceneId());
        }

        boundLine.getStartPointConnectionObject().getBoundLines().remove(boundLine);
        boundLine.getStartPointConnectionObject().calculateTooltip();
    }

    //TODO both these methods really need to be cleaned and streamlined
    private void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        final JOIComponent component = loadJOIService.getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());

        //Process where to add the jump to
        if (outputConnection.getId().contains("dialog_option")) {
            final DialogOption dialogOption = ((Scene) component).getComponent(Dialog.class).getOptionArrayList().get(outputConnection.getOptionNumber());

            if (dialogOption.getGotoScene() == null) dialogOption.setGotoScene(new GotoScene());

            dialogOption.getGotoScene().addValue(inputConnection.getParentSceneId());
        } else {
            if (component instanceof Scene) {
                GotoScene gotoScene;
                try {
                    gotoScene = ((Scene) component).getComponent(GotoScene.class);
                } catch (NoSuchElementException e) {
                    gotoScene = new GotoScene();
                }

                gotoScene.addValue(inputConnection.getParentSceneId());
                ((Scene) component).addComponent(gotoScene);

            } else if (component instanceof VariableSetter) {
                GotoScene gotoScene = ((VariableSetter) component).getGotoScene();
                if (gotoScene == null) gotoScene = new GotoScene();
                gotoScene.addValue(inputConnection.getParentSceneId());

                ((VariableSetter) component).setGotoScene(gotoScene);

            } else if (component instanceof Arithmetic) {
                GotoScene gotoScene = ((Arithmetic) component).getGotoScene();
                if (gotoScene == null) gotoScene = new GotoScene();
                gotoScene.addValue(inputConnection.getParentSceneId());

                ((Arithmetic) component).setGotoScene(gotoScene);

            } else if (component instanceof Condition) {
                if (outputConnection.getId().equals("true_output")) {
                    GotoScene gotoScene = ((Condition) component).getGotoSceneTrue();
                    if (gotoScene == null) gotoScene = new GotoScene();
                    gotoScene.addValue(inputConnection.getParentSceneId());

                    ((Condition) component).setGotoSceneTrue(gotoScene);
                } else if (outputConnection.getId().equals("false_output")) {
                    GotoScene gotoScene = ((Condition) component).getGotoSceneFalse();
                    if (gotoScene == null) gotoScene = new GotoScene();
                    gotoScene.addValue(inputConnection.getParentSceneId());

                    ((Condition) component).setGotoSceneFalse(gotoScene);
                }
            }
        }
    }

    private void removeConnectionFromStory(AsisConnectionButton outputConnection, int inputSceneId) {
        final JOIComponent joiComponent = loadJOIService.getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());

        if (outputConnection.getId().contains("dialog_option")) {
            //Remove from inner dialog location
            try {
                DialogOption dialogOption = ((Scene) joiComponent).getComponent(Dialog.class).getOptionArrayList()
                        .get(outputConnection.getOptionNumber());

                if (outputConnection.getBoundLines().size() > 1) {
                    dialogOption.getGotoScene().removeValue(inputSceneId);

                } else {
                    dialogOption.setGotoScene(null);
                }
            } catch (NullPointerException ignore) {}
        } else {
            if (outputConnection.getBoundLines().size() > 1) {

                GotoScene gotoScene;
                if (joiComponent instanceof Scene) {
                    gotoScene = ((Scene) joiComponent).getComponent(GotoScene.class);
                } else if (joiComponent instanceof VariableSetter) {
                    gotoScene = ((VariableSetter) joiComponent).getGotoScene();
                } else if (joiComponent instanceof Arithmetic) {
                    gotoScene = ((Arithmetic) joiComponent).getGotoScene();
                } else if (joiComponent instanceof Condition) {
                    if (outputConnection.getId().equals("true_output")) {
                        gotoScene = ((Condition) joiComponent).getGotoSceneTrue();
                    } else if (outputConnection.getId().equals("false_output")) {
                        gotoScene = ((Condition) joiComponent).getGotoSceneFalse();
                    } else {
                        throw new RuntimeException("connection id was not true or false!");
                    }
                } else {
                    return;
                }
                gotoScene.removeValue(inputSceneId);

            } else {
                if (joiComponent instanceof Scene) {
                    ((Scene) joiComponent).removeComponent(GotoScene.class);
                } else if (joiComponent instanceof VariableSetter) {
                    ((VariableSetter) joiComponent).setGotoScene(null);
                } else if (joiComponent instanceof Arithmetic) {
                    ((Arithmetic) joiComponent).setGotoScene(null);
                } else if (joiComponent instanceof Condition) {
                    if (outputConnection.getId().equals("true_output")) {
                        ((Condition) joiComponent).setGotoSceneTrue(null);
                    } else if (outputConnection.getId().equals("false_output")) {
                        ((Condition) joiComponent).setGotoSceneFalse(null);
                    }
                }
            }
        }
    }
}
