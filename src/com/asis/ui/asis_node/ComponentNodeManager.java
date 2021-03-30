package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.controllers.NodeGroupWindow;
import com.asis.controllers.dialogs.DialogSceneTitle;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.Group;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Scene;
import com.asis.ui.asis_node.node_group.NodeGroup;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Draggable;
import javafx.geometry.Point2D;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Handles creating component nodes
 */
public class ComponentNodeManager {

    private final ArrayList<JOIComponentNode> joiComponentNodes = new ArrayList<>();
    private final EditorWindow editorWindow;

    public double menuEventX;
    public double menuEventY;
    public boolean calledFromContextMenu = false;

    public ComponentNodeManager(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    private void initializeComponentNode(JOIComponentNode componentNode, double xPosition, double yPosition, String title,
                                         int componentId, boolean suppressJSONUpdating) {
        new Draggable.Nature(componentNode);
        componentNode.setTitle(title);

        //Set and save position
        if (!calledFromContextMenu) {
            componentNode.positionInGrid(xPosition, yPosition);

            if (!suppressJSONUpdating) {
                getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(xPosition);
                getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(yPosition);
            }
        } else {
            Point2D placementCoordinates = editorWindow.getInfinityPane().sceneToWorld(menuEventX, menuEventY);

            componentNode.positionInGrid(placementCoordinates.getX(), placementCoordinates.getY());
            calledFromContextMenu = false;

            //No suppress check because block only gets run from context menu
            getJoiPackage().getJoi().getComponent(componentId).setLayoutXPosition(placementCoordinates.getX());
            getJoiPackage().getJoi().getComponent(componentId).setLayoutYPosition(placementCoordinates.getY());
        }

        componentNode.toBack();
        editorWindow.getInfinityPane().getContainer().getChildren().add(componentNode);
        getJoiComponentNodes().add(componentNode);
    }

    public void addGroup() {
        final int sceneId = getJoiPackage().getJoi().getSceneIdCounter() + 1;
        final String defaultTitle = "Group " + sceneId;

        String title = DialogSceneTitle.addNewSceneDialog(defaultTitle, "Node Group Title");
        if (title == null) return;

        addJOIComponentNode(NodeGroup.class, Group.class, 10, 0, title, sceneId - 1, false);
    }

    public void addScene(final boolean isFirstScene) {
        final int sceneId = getJoiPackage().getJoi().getSceneIdCounter() + 1;
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
        final int componentId = getJoiPackage().getJoi().getSceneIdCounter();
        addJOIComponentNode(componentNodeClass, componentClass, 0, 10, null, componentId, false);
    }

    public void addJOIComponentNode(Class<? extends JOIComponentNode> componentNodeClass, Class<? extends JOIComponent> componentClass,
                                    double xPosition, double yPosition, String title, int componentId, boolean suppressJSONUpdating) {
        //Add new scene to json if not suppressed
        if (!suppressJSONUpdating) {
            getJoiPackage().getJoi().addNewComponent(componentClass, componentId);
            getJoiPackage().getJoi().getComponent(componentId).setComponentTitle(title);

            if(editorWindow instanceof NodeGroupWindow)
                getJoiPackage().getJoi().getComponent(componentId).setGroupId(((NodeGroupWindow) editorWindow).getGroup().getComponentId());
        }

        try {
            JOIComponentNode componentNode = componentNodeClass
                    .getConstructor(int.class, int.class, int.class, JOIComponent.class, EditorWindow.class)
                    .newInstance(300, 100, componentId, getJoiPackage().getJoi().getComponent(componentId), editorWindow);
            initializeComponentNode(componentNode, xPosition, yPosition, title, componentId, suppressJSONUpdating);

            if (!suppressJSONUpdating && !(componentNode instanceof SceneNode)) {
                componentNode.setVisible(false);
                boolean result = componentNode.openDialog();

                if (!result) componentNode.removeComponentNode(componentNode);
                else componentNode.setVisible(true);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    private JOIPackage getJoiPackage() {
        return Controller.getInstance().getJoiPackage();
    }

    public ArrayList<JOIComponentNode> getJoiComponentNodes() {
        return joiComponentNodes;
    }

    public JOIComponentNode getJOIComponentNodeWithId(int componentId) {
        ArrayList<JOIComponentNode> components = getJoiComponentNodes();
        for (JOIComponentNode componentNode : components)
            if (componentNode.getComponentId() == componentId) return componentNode;
        return null;
    }

}
