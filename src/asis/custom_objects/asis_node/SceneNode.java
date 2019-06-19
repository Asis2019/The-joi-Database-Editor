package asis.custom_objects.asis_node;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class SceneNode extends Region {
    //TODO make border pane 0.75 opacity

    private BorderPane borderPane = new BorderPane();
    private SceneNodeMainController sceneNodeMainController;
    private VBox outputContainer = new VBox();
    private VBox inputContainer = new VBox();
    private Label titleLabel = new Label("Title");
    private int sceneId;
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController) {
        this.sceneId = sceneId;
        this.sceneNodeMainController = sceneNodeMainController;

        initializeVBoxes();

        borderPane.setMinSize(width, height);

        titleLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 25px;"
        );

        borderPane.setStyle(
                "-fx-background-color: #5a5a5a;" +
                "-fx-background-radius: 10;" +
                "-fx-background-insets: 8;" +
                "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                "-fx-opacity: 1;"
        );

        borderPane.setCenter(titleLabel);

        contextMenu();

        if(sceneId != -1) {
            if(sceneId != 0) {
                createNewInputConnectionPoint();
            }

            createNewOutputConnectionPoint("Default", "normal_output");
        }
    }

    List<AsisConnectionButton> getOutputButtons() {
        return this.outputConnections;
    }

    AsisConnectionButton getInputConnection() {
        return this.inputConnection;
    }

    private void contextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Edit");
        menuItem1.setOnAction(event -> {
            //do stuff
        });
        contextMenu.getItems().add(menuItem1);
        borderPane.setOnContextMenuRequested(contextMenuEvent ->  {
            contextMenu.show(borderPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }

    private void createNewInputConnectionPoint() {
        inputConnection = new AsisConnectionButton(sceneNodeMainController.getPane(), true, sceneId);

        //Add button to lookup list
        sceneNodeMainController.addInputConnection(inputConnection);


        inputContainer.getChildren().add(inputConnection);
    }

    public AsisConnectionButton createNewOutputConnectionPoint(String labelText, String connectionId) {
        AsisConnectionButton connection = new AsisConnectionButton(sceneNodeMainController.getPane(), false, sceneId);
        attachHandlers(connection);

        Label connectionLabel = new Label(labelText);
        connectionLabel.setTextFill(Color.WHITE);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(connectionLabel, connection);

        //Add button to list
        outputConnections.add(connection);

        connection.setConnectionId(connectionId);

        outputContainer.getChildren().add(hBox);

        return connection;
    }

    public void removeOutputConnection() {
        outputContainer.getChildren().remove(outputConnections.size()-1);
        outputConnections.remove(outputConnections.size()-1);
    }

    private void attachHandlers(AsisConnectionButton connection) {
        connection.setOnMouseMoved(e -> sceneNodeMainController.mouseMoved(e));

        connection.setOnMouseDragged(e -> sceneNodeMainController.mouseMoved(e));

        connection.setOnMousePressed(e -> sceneNodeMainController.mousePressed(connection));

        connection.setOnMouseReleased(e -> sceneNodeMainController.mouseReleased(e));
    }

    private void initializeVBoxes() {
        outputContainer.setAlignment(Pos.CENTER_RIGHT);
        outputContainer.setSpacing(5);
        outputContainer.setPadding(new Insets(20, 0, 20, 0));
        borderPane.setRight(outputContainer);

        inputContainer.setAlignment(Pos.CENTER_LEFT);
        borderPane.setLeft(inputContainer);

    }

    public int getSceneId() {
        return sceneId;
    }

    public Pane getPane() {
        return borderPane;
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
