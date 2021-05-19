package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ComponentConnectionManager {
    private final DoubleProperty mouseX = new SimpleDoubleProperty();
    private final DoubleProperty mouseY = new SimpleDoubleProperty();

    private AsisConnectionButton currentlyActiveConnection = null;
    private final EditorWindow editorWindow;

    public ComponentConnectionManager(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
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

        editorWindow.getInfinityPane().getContainer().getChildren().add(0, boundLine);

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
        final double menuBarOffset = editorWindow.getMenuHeight();

        Point2D placementCoordinates = editorWindow.getInfinityPane().sceneToWorld(mouseEvent.getSceneX(), mouseEvent.getSceneY());

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

        editorWindow.getInfinityPane().getContainer().getChildren().add(0, connectionLine);
    }

    /**
     * Removes the passed line from the node hierarchy and performs cleanup operations.
     *
     * @param boundLine - the line to remove
     */
    private void removeLine(BoundLine boundLine) {
        editorWindow.getInfinityPane().getContainer().getChildren().remove(boundLine);

        if (boundLine.getEndPointConnectionObject() != null) {
            boundLine.getEndPointConnectionObject().getBoundLines().remove(boundLine);
            boundLine.getEndPointConnectionObject().calculateTooltip();

            removeConnectionFromStory(boundLine.getStartPointConnectionObject(), boundLine.getEndPointConnectionObject().getParentSceneId());
        }

        boundLine.getStartPointConnectionObject().getBoundLines().remove(boundLine);
        boundLine.getStartPointConnectionObject().calculateTooltip();
    }

    private void addConnectionToStory(AsisConnectionButton outputConnection, AsisConnectionButton inputConnection) {
        final JOIComponent component = Controller.getInstance().getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());
        if(component == null) return;

        //Process where to add the jump to
        if (outputConnection.getId().contains("dialog_option")) {
            final DialogOption dialogOption = ((Scene) component).getComponent(Dialog.class).getOptionArrayList().get(outputConnection.getOptionNumber());

            if (dialogOption.getGotoScene() == null) dialogOption.setGotoScene(new GotoScene());

            dialogOption.getGotoScene().addValue(inputConnection.getParentSceneId());
        } else {
            component.accept(new AddConnectionResolver(outputConnection.getId(), inputConnection.getParentSceneId()));
        }
    }

    private void removeConnectionFromStory(AsisConnectionButton outputConnection, int inputSceneId) {
        final JOIComponent joiComponent = Controller.getInstance().getJoiPackage().getJoi().getComponent(outputConnection.getParentSceneId());
        if(joiComponent == null) return;

        final boolean isMultiLined = outputConnection.getBoundLines().size() > 1;

        if (outputConnection.getId().contains("dialog_option")) {
            //Remove from inner dialog location
            try {
                DialogOption dialogOption = ((Scene) joiComponent).getComponent(Dialog.class).getOptionArrayList()
                        .get(outputConnection.getOptionNumber());

                if (isMultiLined) dialogOption.getGotoScene().removeValue(inputSceneId);
                else dialogOption.setGotoScene(null);
            } catch (NullPointerException ignore) {}
        } else {
            joiComponent.accept(new RemoveConnectionResolver(isMultiLined, inputSceneId, outputConnection.getId()));
        }
    }

    private static class RemoveConnectionResolver implements ComponentVisitor {
        final boolean isMultiLined;
        final int inputComponentId;
        final String outputConnectionId;

        public RemoveConnectionResolver(boolean isMultiLined, int inputComponentId, String outputConnectionId) {
            this.isMultiLined = isMultiLined;
            this.inputComponentId = inputComponentId;
            this.outputConnectionId = outputConnectionId;
        }

        @Override
        public void visit(Scene scene) {
            if(isMultiLined)
                scene.getComponent(GotoScene.class).removeValue(inputComponentId);
            else
                scene.removeComponent(GotoScene.class);
        }

        @Override
        public void visit(Condition condition) {
            if(isMultiLined) {
                if (outputConnectionId.equals("true_output"))
                    condition.getGotoSceneTrue().removeValue(inputComponentId);
                else if (outputConnectionId.equals("false_output"))
                    condition.getGotoSceneFalse().removeValue(inputComponentId);
                else throw new RuntimeException("connection id was not true or false!");
            } else {
                if (outputConnectionId.equals("true_output")) condition.setGotoSceneTrue(null);
                else if (outputConnectionId.equals("false_output")) condition.setGotoSceneFalse(null);
            }
        }

        @Override
        public void visit(VariableSetter variableSetter) {
            if(isMultiLined)
                variableSetter.getGotoScene().removeValue(inputComponentId);
            else
                variableSetter.setGotoScene(null);
        }

        @Override
        public void visit(Arithmetic arithmetic) {
            if(isMultiLined)
                arithmetic.getGotoScene().removeValue(inputComponentId);
            else
                arithmetic.setGotoScene(null);
        }

        @Override
        public void visit(Group group) {
            if(isMultiLined)
                group.getGotoScene().removeValue(inputComponentId);
            else
                group.setGotoScene(null);
        }

        @Override
        public void visit(GroupBridge groupBridge) {
            if(isMultiLined)
                groupBridge.getGotoScene().removeValue(inputComponentId);
            else
                groupBridge.setGotoScene(null);
        }
    }

    private static class AddConnectionResolver implements ComponentVisitor {

        final String outputId;
        final int parentSceneId;

        public AddConnectionResolver(String outputId, int parentSceneId) {
            this.outputId = outputId;
            this.parentSceneId = parentSceneId;
        }

        @Override
        public void visit(Scene scene) {
            GotoScene gotoScene;
            try {
                gotoScene = scene.getComponent(GotoScene.class);
            } catch (NoSuchElementException e) {
                gotoScene = new GotoScene();
            }

            gotoScene.addValue(parentSceneId);
            scene.addComponent(gotoScene);
        }

        @Override
        public void visit(Condition condition) {
            if (outputId.equals("true_output")) {
                GotoScene gotoScene = condition.getGotoSceneTrue();
                if (gotoScene == null) gotoScene = new GotoScene();
                gotoScene.addValue(parentSceneId);

                condition.setGotoSceneTrue(gotoScene);
            } else if (outputId.equals("false_output")) {
                GotoScene gotoScene = condition.getGotoSceneFalse();
                if (gotoScene == null) gotoScene = new GotoScene();
                gotoScene.addValue(parentSceneId);

                condition.setGotoSceneFalse(gotoScene);
            }
        }

        @Override
        public void visit(VariableSetter variableSetter) {
            GotoScene gotoScene = variableSetter.getGotoScene();
            if (gotoScene == null) gotoScene = new GotoScene();
            gotoScene.addValue(parentSceneId);

            variableSetter.setGotoScene(gotoScene);
        }

        @Override
        public void visit(Arithmetic arithmetic) {
            GotoScene gotoScene = arithmetic.getGotoScene();
            if (gotoScene == null) gotoScene = new GotoScene();
            gotoScene.addValue(parentSceneId);

            arithmetic.setGotoScene(gotoScene);
        }

        @Override
        public void visit(Group group) {
            GotoScene gotoScene = group.getGotoScene();
            if (gotoScene == null) gotoScene = new GotoScene();
            gotoScene.addValue(parentSceneId);

            group.setGotoScene(gotoScene);
        }

        @Override
        public void visit(GroupBridge groupBridge) {
            GotoScene gotoScene = groupBridge.getGotoScene();
            if (gotoScene == null) gotoScene = new GotoScene();
            gotoScene.addValue(parentSceneId);

            groupBridge.setGotoScene(gotoScene);
        }
    }
}
