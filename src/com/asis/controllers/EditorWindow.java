package com.asis.controllers;

import com.asis.joi.model.entities.Arithmetic;
import com.asis.joi.model.entities.Condition;
import com.asis.joi.model.entities.VariableSetter;
import com.asis.ui.InfinityPane;
import com.asis.ui.asis_node.*;
import com.asis.utilities.StageManager;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public abstract class EditorWindow {

    private final ContextMenu editorWindowContextMenu = new ContextMenu();

    protected ComponentNodeManager nodeManager = new ComponentNodeManager(this);
    protected ComponentConnectionManager connectionManager = new ComponentConnectionManager(this);

    public void initialize() {
        getInfinityPane().setUserData(this);
        setupInfinityPaneContextMenu();
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
    protected void setupInfinityPaneContextMenu() {
        //Create items and add them to there menu
        MenuItem newSceneItem = new MenuItem("New Scene");
        MenuItem newVariableSetterItem = new MenuItem("New Variable");
        MenuItem newConditionItem = new MenuItem("New Condition");
        MenuItem newArithmeticItem = new MenuItem("New Arithmetic");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem reset_view = new MenuItem("Reset view");
        editorWindowContextMenu.getItems().addAll(newSceneItem, newVariableSetterItem, newConditionItem,
                newArithmeticItem, separatorMenuItem, reset_view);

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
     * Forces all editor windows to have an infinity pane implementation.
     * @return InfinityPane - the editor window's infinity pane
     */
    public abstract InfinityPane getInfinityPane();

    public ComponentConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public ComponentNodeManager getNodeManager() {
        return nodeManager;
    }
}
