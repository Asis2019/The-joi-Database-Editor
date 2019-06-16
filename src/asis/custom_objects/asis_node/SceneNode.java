package asis.custom_objects.asis_node;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class SceneNode extends Region {
    //TODO make border pane 0.75 opacity

    private BorderPane borderPane = new BorderPane();
    private SceneNodeMainController sceneNodeMainController;
    private VBox outputContainer = new VBox();
    private VBox inputContainer = new VBox();
    private Label titleLabel = new Label("Title");
    private String sceneId;
    private List<AsisConnectionButton> outputConnections = new ArrayList<>();
    private AsisConnectionButton inputConnection;

    public SceneNode(int width, int height, String sceneId, SceneNodeMainController sceneNodeMainController) {
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

        if(!sceneId.equals("metaData")) {
            if(!sceneId.equals("1")) {
                createNewInputConnectionPoint();
            }

            createNewOutputConnectionPoint();
        }
    }

    private void createNewOutputConnectionPoint() {
        AsisConnectionButton connection = new AsisConnectionButton(sceneNodeMainController.getPane(), false, sceneId);
        attachHandlers(connection);

        //Add button to list
        outputConnections.add(connection);

        connection.setConnectionId(sceneId+"_"+outputConnections.size());

        outputContainer.getChildren().add(connection);

    }

    private void createNewInputConnectionPoint() {
        inputConnection = new AsisConnectionButton(sceneNodeMainController.getPane(), true, sceneId);

        //Add button to lookup list
        sceneNodeMainController.addInputConnection(inputConnection);


        inputContainer.getChildren().add(inputConnection);
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

    public String getSceneId() {
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
