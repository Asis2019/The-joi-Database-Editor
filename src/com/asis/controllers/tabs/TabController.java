package com.asis.controllers.tabs;

import com.asis.controllers.Controller;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Line;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneComponent;
import com.asis.ui.ImageViewPane;
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

    TabController(String tabTitle) {
        setTabTitle(tabTitle);
    }

    void setVisibleImage(StackPane stackPane, ImageViewPane viewPane, File workingFile) {
        //Remove image if any is present
        stackPane.getChildren().remove(viewPane);

        //Make image visible
        Image image = new Image(workingFile.toURI().toString());
        ImageView sceneImageView = new ImageView();
        sceneImageView.setImage(image);
        sceneImageView.setPreserveRatio(true);
        viewPane.setImageView(sceneImageView);
        stackPane.getChildren().add(0, viewPane);
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
}
