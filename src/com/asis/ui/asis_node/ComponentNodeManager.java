package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogArithmetic;
import com.asis.controllers.dialogs.DialogCondition;
import com.asis.controllers.dialogs.DialogSceneTitle;
import com.asis.controllers.dialogs.DialogVariableSetter;
import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.utilities.Draggable;
import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Handles creating component nodes
 */
public class ComponentNodeManager {

    private static ComponentNodeManager componentNodeManager = new ComponentNodeManager();
    private final Controller controller = Controller.getInstance();
    private final ArrayList<JOIComponentNode> joiComponentNodes = new ArrayList<>();
    
    private ComponentNodeManager() {
        if (componentNodeManager == null) componentNodeManager = this;
    }

    private void initializeComponentNode(JOIComponentNode componentNode, double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        new Draggable.Nature(componentNode);
        componentNode.setTitle(title);

        //Set and save position
        if (!controller.addSceneContextMenu) {
            //TODO issue 5 make new scenes via button adjacent
            componentNode.positionInGrid(xPosition, yPosition);

            if (!suppressJSONUpdating) {
                controller.getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(xPosition);
                controller.getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(yPosition);
            }
        } else {
            Point2D placementCoordinates = controller.getInfinityPane().sceneToWorld(controller.menuEventX, controller.menuEventY);

            componentNode.positionInGrid(placementCoordinates.getX(), placementCoordinates.getY());
            controller.addSceneContextMenu = false;

            //No suppress check because block only gets run from context menu
            controller.getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(placementCoordinates.getX());
            controller.getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(placementCoordinates.getY());
        }

        componentNode.toBack();
        controller.getInfinityPane().getContainer().getChildren().add(componentNode);
        getJoiComponentNodes().add(componentNode);
    }

    public void addArithmeticNode() {
        final int componentId = controller.getJoiPackage().getJoi().getSceneIdCounter();
        addArithmeticNode(0, 10, null, componentId, false);
    }

    public void addArithmeticNode(double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        if (!suppressJSONUpdating) {
            controller.getJoiPackage().getJoi().addNewComponent(Arithmetic.class, componentId);
            controller.getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);
        }

        JOIComponentNode componentNode = new ArithmeticNode(300, 100, componentId, controller.getJoiPackage().getJoi().getComponent(componentId));
        initializeComponentNode(componentNode, xPosition, yPosition, title, componentId, suppressJSONUpdating);

        if (!suppressJSONUpdating) {
            componentNode.setVisible(false);
            boolean result = DialogArithmetic.openArithmetic((Arithmetic) componentNode.getJoiComponent());

            if (!result) removeComponentNode(componentNode);
            else componentNode.setVisible(true);
        }
    }

    public void addConditionNode() {
        final int componentId = controller.getJoiPackage().getJoi().getSceneIdCounter();
        addConditionNode(0, 10, null, componentId, false);
    }

    public void addConditionNode(double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        if (!suppressJSONUpdating) {
            controller.getJoiPackage().getJoi().addNewComponent(Condition.class, componentId);
            controller.getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);
        }

        JOIComponentNode componentNode = new ConditionNode(300, 100, componentId, controller.getJoiPackage().getJoi().getComponent(componentId));
        initializeComponentNode(componentNode, xPosition, yPosition, title, componentId, suppressJSONUpdating);

        if (!suppressJSONUpdating) {
            componentNode.setVisible(false);
            boolean result = DialogCondition.openConditionDialog((Condition) componentNode.getJoiComponent());

            if (!result) removeComponentNode(componentNode);
            else componentNode.setVisible(true);
        }
    }

    public void addVariableSetterNode() {
        final int componentId = controller.getJoiPackage().getJoi().getSceneIdCounter();
        addVariableSetterNode(0, 10, null, componentId, false);
    }

    public void addVariableSetterNode(double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        if (!suppressJSONUpdating) {
            controller.getJoiPackage().getJoi().addNewComponent(VariableSetter.class, componentId);
            controller.getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);
        }

        JOIComponentNode componentNode = new VariableSetterNode(300, 100, componentId, controller.getJoiPackage().getJoi().getComponent(componentId));
        initializeComponentNode(componentNode, xPosition, yPosition, title, componentId, suppressJSONUpdating);

        if (!suppressJSONUpdating) {
            componentNode.setVisible(false);
            boolean result = DialogVariableSetter.openVariableSetter((VariableSetter) componentNode.getJoiComponent());

            if (!result) removeComponentNode(componentNode);
            else componentNode.setVisible(true);
        }
    }

    public void addScene(final boolean isFirstScene) {
        final int sceneId = controller.getJoiPackage().getJoi().getSceneIdCounter() + 1;
        final String defaultTitle = "Scene " + sceneId;
        String title;

        if (isFirstScene) {
            title = defaultTitle;
        } else {
            title = DialogSceneTitle.addNewSceneDialog(defaultTitle);
            if (title == null) return;
        }

        addScene(10, 0, title, sceneId - 1, false);
    }

    public void addScene(double xPosition, double yPosition, String title, int sceneId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            controller.getJoiPackage().getJoi().addNewComponent(com.asis.joi.model.entities.Scene.class, sceneId);
            controller.getJoiPackage().getJoi().getComponent(sceneId).setComponentTitle(title);
        }

        SceneNode sceneNode = new SceneNode(300, 100, sceneId, (com.asis.joi.model.entities.Scene) controller.getJoiPackage().getJoi().getComponent(sceneId));
        initializeComponentNode(sceneNode, xPosition, yPosition, title, sceneId, suppressJSONUpdating);
    }

    public void removeComponentNode(JOIComponentNode joiComponentNode) {
        controller.getJoiPackage().getJoi().removeComponent(joiComponentNode.getComponentId());
        ComponentConnectionManager.getInstance().removeConnection(joiComponentNode);
        controller.getInfinityPane().getContainer().getChildren().remove(joiComponentNode);
    }

    public static ComponentNodeManager getInstance() {
        return componentNodeManager;
    }

    public ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return joiComponentNodes;
    }
}
