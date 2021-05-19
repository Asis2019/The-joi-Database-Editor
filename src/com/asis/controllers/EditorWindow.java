package com.asis.controllers;

import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.*;
import com.asis.utilities.Config;
import com.asis.utilities.SelectionModel;
import com.asis.utilities.StageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.stage.Stage;
import org.json.JSONObject;

import static com.asis.utilities.AsisUtils.screenToStage;

public abstract class EditorWindow {

    private final ContextMenu editorWindowContextMenu = new ContextMenu();
    private final SelectionModel selectionModel = new SelectionModel();

    protected ComponentNodeManager nodeManager = new ComponentNodeManager(this);
    protected ComponentConnectionManager connectionManager = new ComponentConnectionManager(this);

    protected boolean snapToGrid = false;
    protected boolean showThumbnail = false;

    @FXML
    public ToolBar toolBar;
    @FXML
    protected MenuBar mainMenuBar;
    @FXML
    protected Button gridToggle, thumbnailToggle;

    public void initialize() {
        getInfinityPane().setUserData(this);
        setupInfinityPaneContextMenu();
        attachStageEventHandlers();

        try {
            JSONObject object = (JSONObject) Config.get("ZOOM");
            if (object.has("minimum")) getInfinityPane().setMinimumScale(object.getDouble("minimum"));
            if (object.has("maximum")) getInfinityPane().setMaximumScale(object.getDouble("maximum"));
        } catch (ClassCastException ignore) {}
    }

    private void attachStageEventHandlers() {
        Platform.runLater(() -> {
            getStage().setUserData("editor-window");
            Stage stage = (Stage) getInfinityPane().getScene().getWindow();

            stage.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            if (((JOIComponentNode)event.getGestureSource()).getScene().getWindow() != stage) {
                SelectionModel selectionModel = ((JOIComponentNode)event.getGestureSource()).getEditorWindow().getSelectionModel();
                for(Node transferNode: selectionModel.getSelection()) {
                    if (transferNode != null) {
                        getInfinityPane().getContainer().getChildren().add(transferNode);

                        Point2D stageCoordinates = screenToStage(stage, new Point2D(event.getScreenX(), event.getScreenY()));
                        Point2D paneTransformedCoordinates = getInfinityPane().getContainer().sceneToLocal(stageCoordinates);

                        ((JOIComponentNode) transferNode).positionInGrid(
                                paneTransformedCoordinates.getX(),
                                paneTransformedCoordinates.getY());

                        event.consume();
                    }
                }
            }
            });
        });
    }

    /**
     * Clears all nodes and connections from the editor panel. And close all related windows.
     */
    protected void resetEditorWindow() {
        getInfinityPane().getContainer().getChildren().clear();
        getNodeManager().getJoiComponentNodes().clear();
        StageManager.getInstance().closeAllStages();
    }

    /**
     * Creates and sets the context menu for the editor windows infinity pane.
     */
    private void setupInfinityPaneContextMenu() {
        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        MenuItem newVariableSetterItem = new MenuItem("New Variable");
        MenuItem newConditionItem = new MenuItem("New Condition");
        MenuItem newArithmeticItem = new MenuItem("New Arithmetic");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem newNodeGroupItem = new MenuItem("New Node Group");
        SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();

        MenuItem reset_view = new MenuItem("Reset view");
        editorWindowContextMenu.getItems().addAll(newSceneItem, newVariableSetterItem, newConditionItem,
                newArithmeticItem, separatorMenuItem, newNodeGroupItem, separatorMenuItem2, reset_view);

        //Handle menu actions
        newSceneItem.setOnAction(event -> {
            getNodeManager().calledFromContextMenu = true;
            getNodeManager().addScene(false);
        });
        newVariableSetterItem.setOnAction(actionEvent -> {
            getNodeManager().calledFromContextMenu = true;
            getNodeManager().addJOIComponentNode(VariableSetterNode.class, VariableSetter.class);
        });
        newConditionItem.setOnAction(actionEvent -> {
            getNodeManager().calledFromContextMenu = true;
            getNodeManager().addJOIComponentNode(ConditionNode.class, Condition.class);
        });
        newArithmeticItem.setOnAction(actionEvent -> {
            getNodeManager().calledFromContextMenu = true;
            getNodeManager().addJOIComponentNode(ArithmeticNode.class, Arithmetic.class);
        });
        newNodeGroupItem.setOnAction(actionEvent -> {
            getNodeManager().calledFromContextMenu = true;
            getNodeManager().addGroup();
        });
        reset_view.setOnAction(actionEvent -> getInfinityPane().resetPosition());

        getInfinityPane().setContextMenu(editorWindowContextMenu);
        getInfinityPane().setOnContextMenuRequested(contextMenuEvent -> {
            if (!getInfinityPane().nodeAtPosition(contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY())) {
                editorWindowContextMenu.show(getInfinityPane(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }

            getNodeManager().menuEventX = contextMenuEvent.getX();
            getNodeManager().menuEventY = contextMenuEvent.getY();
        });
    }

    /**
     * Toggles the visibility of scene thumbnails.
     *
     * @param showThumbnail - boolean whether to show the thumbnail or not
     */
    public void toggleSceneThumbnails(boolean showThumbnail) {
        getNodeManager().getJoiComponentNodes().forEach(joiComponentNode -> {
            if (joiComponentNode instanceof SceneNode)
                ((SceneNode) joiComponentNode).toggleSceneThumbnail(showThumbnail);
        });
    }

    @FXML
    protected void actionToggleGrid() {
        snapToGrid = !snapToGrid;
        ImageView imageView;
        if (snapToGrid)
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_on.png")));
        else
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_grid_off.png")));

        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        gridToggle.setGraphic(imageView);
    }

    @FXML
    protected void actionToggleThumbnail() {
        showThumbnail = !showThumbnail;
        ImageView imageView;
        if (showThumbnail)
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_thumbnail_on.png")));
        else
            imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/ic_thumbnail_off.png")));

        toggleSceneThumbnails(showThumbnail);

        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        thumbnailToggle.setGraphic(imageView);
    }

    /**
     * Forces all editor windows to have an infinity pane implementation.
     *
     * @return InfinityPane - the editor window's infinity pane
     */
    public abstract InfinityPane getInfinityPane();

    public ComponentConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public ComponentNodeManager getNodeManager() {
        return nodeManager;
    }

    protected Stage getStage() {
        return (Stage) getInfinityPane().getScene().getWindow();
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public boolean isShowThumbnail() {
        return showThumbnail;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    public double getMenuHeight() {
        return toolBar.getHeight() + mainMenuBar.getHeight();
    }
}
