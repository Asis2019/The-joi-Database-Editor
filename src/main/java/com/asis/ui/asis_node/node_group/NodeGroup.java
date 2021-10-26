package com.asis.ui.asis_node.node_group;

import com.asis.Main;
import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.controllers.NodeGroupWindow;
import com.asis.joi.model.entities.Group;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.ui.asis_node.JOIComponentNode;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class NodeGroup extends JOIComponentNode {

    public NodeGroup(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(width, height, componentId, component, editorWindow);

        setUserData("group");
        setId("NodeGroup");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint(null, "normal_output");

        setupContextMenu();
    }

    public static void openNodeGroupWindow(NodeGroup nodeGroup) {
        if (StageManager.getInstance().requestStageFocus(nodeGroup.getComponentId())) return;

        //Open new window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/node_group_window.fxml"));
            Parent root = fxmlLoader.load();

            NodeGroupWindow nodeGroupWindow = fxmlLoader.getController();
            nodeGroupWindow.initialize((Group) nodeGroup.getJoiComponent());

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(nodeGroup.getTitle());
            stage.setUserData(nodeGroup.getComponentId());
            stage.setScene(new javafx.scene.Scene(root, 1280, 720));

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a, #273036, #273036, #273036, #5a5a5a;" +
                            "-fx-background-radius: 10, 0, 10, 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a, #273036, #273036, #273036, #5a5a5a;" +
                            "-fx-background-radius: 10, 0, 10, 10;" +
                            "-fx-background-insets: 8, 8 17 8 17, 8 22 8 22, 8 27 8 27, 8 32 8 32;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    @Override
    protected boolean openDialog() {
        openNodeGroupWindow(this);
        return true;
    }
}
