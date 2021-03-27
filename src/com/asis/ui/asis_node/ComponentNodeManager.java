package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogSceneTitle;
import com.asis.joi.LoadJOIService;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Scene;
import com.asis.ui.InfinityPane;
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
    private final LoadJOIService loadJOIService = LoadJOIService.getInstance();
    private final ArrayList<JOIComponentNode> joiComponentNodes = new ArrayList<>();

    /**
     * the current infinity pane to add the nodes too
     */
    private InfinityPane workingPane;
    private int workingGroupId;
    
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
                loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(xPosition);
                loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(yPosition);
            }
        } else {
            Point2D placementCoordinates = workingPane.sceneToWorld(controller.menuEventX, controller.menuEventY);

            componentNode.positionInGrid(placementCoordinates.getX(), placementCoordinates.getY());
            controller.addSceneContextMenu = false;

            //No suppress check because block only gets run from context menu
            loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(placementCoordinates.getX());
            loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(placementCoordinates.getY());
        }

        componentNode.toBack();
        workingPane.getContainer().getChildren().add(componentNode);
        getJoiComponentNodes().add(componentNode);
    }

    public void addScene(final boolean isFirstScene) {
        final int sceneId = loadJOIService.getJoiPackage().getJoi().getSceneIdCounter() + 1;
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
        final int componentId = loadJOIService.getJoiPackage().getJoi().getSceneIdCounter();
        addJOIComponentNode(componentNodeClass, componentClass, 0, 10, null, componentId, false);
    }

    public void addJOIComponentNode(Class<? extends JOIComponentNode> componentNodeClass, Class<? extends JOIComponent> componentClass, double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            loadJOIService.getJoiPackage().getJoi().addNewComponent(componentClass, componentId);
            loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);

            loadJOIService.getJoiPackage().getJoi().getComponent(componentId).setGroupId(workingGroupId);
        }

        try {
            JOIComponentNode componentNode = componentNodeClass
                    .getConstructor(int.class, int.class, int.class, JOIComponent.class)
                    .newInstance(300, 100, componentId, loadJOIService.getJoiPackage().getJoi().getComponent(componentId));
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

    public void setWorkingPane(InfinityPane workingPane) {
        this.workingPane = workingPane;
    }

    public void setWorkingGroupId(int workingGroupId) {
        this.workingGroupId = workingGroupId;
    }
}
