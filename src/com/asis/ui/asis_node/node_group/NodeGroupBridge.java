package com.asis.ui.asis_node.node_group;

import com.asis.controllers.EditorWindow;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.JOIComponentNode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * This class adds a way for nodes inside a group to be piped to the group node itself.
 */
public class NodeGroupBridge extends JOIComponentNode {

    private ArrayList<AsisConnectionButton> inputConnections = new ArrayList<>();

    public NodeGroupBridge(int width, int height, int componentId, JOIComponent component, EditorWindow editorWindow) {
        super(width, height, componentId, component, editorWindow);
    }

    public AsisConnectionButton createNewInputConnectionPoint(String labelText) {
        AsisConnectionButton connection = new AsisConnectionButton(true, getJoiComponent(), getEditorWindow().getInfinityPane().getContainer());
        attachHandlers(connection);

        Label connectionLabel = new Label(labelText);
        connectionLabel.setTextFill(Color.WHITE);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(connectionLabel, connection);

        //Add button to list
        inputConnections.add(connection);

        inputContainer.getChildren().add(hBox);

        return connection;
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle("-fx-background-color: #5a5a5a;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-insets: 8;" +
                    "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle("-fx-background-color: #5a5a5a;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-insets: 8;" +
                    "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    @Override
    protected boolean openDialog() {
        return true;
    }

    public ArrayList<AsisConnectionButton> getInputConnections() {
        return inputConnections;
    }
    public void setInputConnections(ArrayList<AsisConnectionButton> inputConnections) {
        this.inputConnections = inputConnections;
    }
}
