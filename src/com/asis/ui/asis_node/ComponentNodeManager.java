package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogSceneTitle;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Scene;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Draggable;
import javafx.geometry.Point2D;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static com.asis.ui.asis_node.JOIComponentNode.removeComponentNode;

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

        addJOIComponentNode(SceneNode.class, Scene.class, 10, 0, title, sceneId - 1, false);
    }

    public void addJOIComponentNode(Class<? extends JOIComponentNode> componentNodeClass, Class<? extends JOIComponent> componentClass) {
        final int componentId = controller.getJoiPackage().getJoi().getSceneIdCounter();
        addJOIComponentNode(componentNodeClass, componentClass, 0, 10, null, componentId, false);
    }

    public void addJOIComponentNode(Class<? extends JOIComponentNode> componentNodeClass, Class<? extends JOIComponent> componentClass, double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            controller.getJoiPackage().getJoi().addNewComponent(componentClass, componentId);
            controller.getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);
        }

        try {
            JOIComponentNode componentNode = componentNodeClass
                    .getConstructor(int.class, int.class, int.class, JOIComponent.class)
                    .newInstance(300, 100, componentId, controller.getJoiPackage().getJoi().getComponent(componentId));
            initializeComponentNode(componentNode, xPosition, yPosition, title, componentId, suppressJSONUpdating);

            if (!suppressJSONUpdating && !(componentNode instanceof SceneNode)) {
                componentNode.setVisible(false);
                boolean result = componentNode.openDialog();

                if (!result) removeComponentNode(componentNode);
                else componentNode.setVisible(true);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public static ComponentNodeManager getInstance() {
        return componentNodeManager;
    }

    public ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return joiComponentNodes;
    }

    public static JOIComponentNode getJOIComponentNodeWithId(int componentId) {
        ArrayList<JOIComponentNode> components = getInstance().getJoiComponentNodes();
        for (JOIComponentNode componentNode : components)
            if (componentNode.getComponentId() == componentId) return componentNode;
        return null;
    }
}
