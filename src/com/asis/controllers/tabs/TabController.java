package com.asis.controllers.tabs;

import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Line;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneComponent;
import com.asis.ui.ImageViewPane;
import com.asis.ui.asis_node.SceneNode;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

public abstract class TabController {
    private boolean isClosable = true;
    private String tabTitle = "";
    private EditorWindow editorWindow;

    TabController(String tabTitle) {
        setTabTitle(tabTitle);
    }

    TabController(String tabTitle, EditorWindow editorWindow) {
        setTabTitle(tabTitle);
        setEditorWindow(editorWindow);
    }

    void setVisibleImage(StackPane stackPane, ImageViewPane viewPane, File workingFile, Scene scene) {
        if(viewPane.getImageFile() != workingFile) {
            //Remove image if any is present
            stackPane.getChildren().remove(viewPane);

            //Make image visible
            Image image = new Image(workingFile.toURI().toString());
            ImageView sceneImageView = new ImageView();
            sceneImageView.setImage(image);
            sceneImageView.setPreserveRatio(true);
            viewPane.setImageView(sceneImageView);
            viewPane.setImageFile(workingFile);
            stackPane.getChildren().add(0, viewPane);

            if(Controller.getInstance().isShowThumbnail()) {
                getEditorWindow().getNodeManager().getJoiComponentNodes().forEach(joiComponentNode -> {
                    if (joiComponentNode.getComponentId() == scene.getComponentId() && joiComponentNode instanceof SceneNode) {
                        ((SceneNode) joiComponentNode).toggleSceneThumbnail(true);
                    }
                });
            }
        }
    }

    void setNodeColorStyle(Node node, String fillColor, String outlineColor) {
        node.setStyle(String.format("outline-color: %s;fill-color: %s;", outlineColor,fillColor));
    }

    <T extends SceneComponent<?>> Scene getScene(T component) {
        for(JOIComponent joiComponent: Controller.getInstance().getJoiPackage().getJoi().getJoiComponents()) {
            if(joiComponent instanceof Scene) {
                if (((Scene) joiComponent).containsComponent(component)) return ((Scene) joiComponent);
            }
        }
        return null;
    }

    static void beatProperties(JSONObject textObject, CheckBox checkBox, String key) {
        checkBox.setSelected(textObject != null && textObject.has(key));
    }

    static void setLineStartCheckBoxState(Line line, CheckBox checkBox) {
        if(line == null) return;

        if(checkBox.isSelected()) {
            line.setStartBeat(true);
        } else {
            line.setStartBeat(null);
        }
    }

    static void setLineStopCheckBoxState(Line line, CheckBox checkBox) {
        if(line == null) return;

        if(checkBox.isSelected()) {
            line.setStopBeat(true);
        } else {
            line.setStopBeat(null);
        }
    }

    static void changeBeatSpeed(JSONObject textObject, TextField textField, String key) {
        if (textObject != null && textObject.has(key)) {
            double speed = textObject.getDouble(key);
            textField.setText(String.valueOf(speed));
        } else {
            textField.clear();
        }
    }

    static String removeLastTwoLetters(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 2))
                .orElse(s);
    }

    //Getters and setters
    public String getTabTitle() {
        return tabTitle;
    }
    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public boolean isClosable() {
        return isClosable;
    }
    public void setClosable(boolean closable) {
        isClosable = closable;
    }

    public EditorWindow getEditorWindow() {
        return editorWindow;
    }
    public void setEditorWindow(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }
}
