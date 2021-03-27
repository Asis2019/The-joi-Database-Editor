package com.asis.controllers;

import com.asis.joi.LoadJOIService;
import com.asis.joi.model.entities.Group;
import com.asis.ui.InfinityPane;
import com.asis.utilities.Config;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import org.json.JSONObject;

public class NodeGroupWindow {

    private Group group;

    @FXML
    private InfinityPane infinityPane;
    @FXML
    public MenuBar mainMenuBar;

    public void initialize(Group group) {
        this.group = group;

        /* Block for context menu of infinity pane. This should be moved somewhere else probably */{
            ContextMenu contextMenu = Controller.buildWorkspaceContextMenu(getInfinityPane(), group.getComponentId());

            infinityPane.setContextMenu(contextMenu);
            infinityPane.setOnContextMenuRequested(contextMenuEvent -> {
                if (!getInfinityPane().nodeAtPosition(contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY())) {
                    contextMenu.show(infinityPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                }

                Controller.getInstance().menuEventX = contextMenuEvent.getX();
                Controller.getInstance().menuEventY = contextMenuEvent.getY();
            });
        }

        try {
            JSONObject object = (JSONObject) Config.get("ZOOM");
            if(object.has("minimum")) getInfinityPane().setMinimumScale(object.getDouble("minimum"));
            if(object.has("maximum")) getInfinityPane().setMaximumScale(object.getDouble("maximum"));
        } catch (ClassCastException ignore) {}

        loadNodes();
    }

    private void loadNodes() {
        LoadJOIService.getInstance().addNodesToPane(getInfinityPane(), group.getComponentId());
    }

    @FXML
    private void actionClose() {
        Stage stage = (Stage) getInfinityPane().getScene().getWindow();
        StageManager.getInstance().closeStage(stage);
    }

    // Getters and setters
    public InfinityPane getInfinityPane() {
        return infinityPane;
    }
}
