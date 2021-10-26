package com.asis.ui.asis_node;

import com.asis.Main;
import com.asis.controllers.Controller;
import com.asis.controllers.EditorWindow;
import com.asis.controllers.SceneDetails;
import com.asis.joi.model.entities.JOIComponent;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneImage;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class SceneNode extends JOIComponentNode {
    private final ImageView imageView = new ImageView();
    private final Rectangle textBackdrop = new Rectangle();

    public SceneNode(int width, int height, int sceneId, JOIComponent scene, EditorWindow editorWindow) {
        super(width, height, sceneId, scene, editorWindow);

        setUserData("sceneNode");
        setId("Scene");

        createNewInputConnectionPoint();
        createNewOutputConnectionPoint("Default", "normal_output");

        initializeEndVariables();
        setupContextMenu();

        imageView.setPreserveRatio(true);
        imageView.setCache(true);
        imageView.setFitWidth(200);
        imageView.setLayoutX(getLayoutX()+10);
        imageView.setLayoutY(getLayoutY()+10);
        imageView.setManaged(false);

        GaussianBlur blur = new GaussianBlur(5);
        imageView.setEffect(blur);


        Rectangle mask = new Rectangle();
        mask.setWidth(200);
        mask.setArcWidth(10);
        mask.setArcHeight(10);
        heightProperty().addListener((observableValue, number, t1) -> {
            mask.setHeight(t1.doubleValue()-20);
            textBackdrop.setY((t1.doubleValue()/2)-(textBackdrop.getHeight()/2));
        });
        imageView.setClip(mask);

        textBackdrop.setFill(Color.BLACK);
        textBackdrop.setOpacity(0.5);
        textBackdrop.setHeight(25);
        textBackdrop.setWidth(200);
        textBackdrop.setX(titleLabel.getLayoutX()+10);
        textBackdrop.setManaged(false);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView, textBackdrop, titleLabel);
        setCenter(stackPane);

        toggleSceneThumbnail(false);
    }

    private void initializeEndVariables() {
        getJOIScene().goodEndProperty().addListener((observableValue, aBoolean, t1) -> changeOutputConnectionsVisibility(!t1));
        getJOIScene().badEndProperty().addListener((observableValue, aBoolean, t1) -> changeOutputConnectionsVisibility(!t1));
        changeOutputConnectionsVisibility(!(getJOIScene().isGoodEnd() || getJOIScene().isBadEnd()));
    }

    public void toggleSceneThumbnail(boolean show) {
        if(show) {
            try {
                textBackdrop.setVisible(true);
                imageView.setVisible(true);
                String sceneImage = getJOIScene().getComponent(SceneImage.class).getImage().toURI().toString();
                Image image = new Image(sceneImage);

                imageView.setImage(image);

                if(imageView.getBoundsInParent().getHeight() < (getHeight()-20)) {
                    imageView.setFitWidth(0);
                    imageView.setFitHeight(getHeight());
                }

                return;
            } catch (NoSuchElementException ignored) {}
        }

        textBackdrop.setVisible(false);
        imageView.setVisible(false);
        imageView.setImage(null);
    }

    public static void openSceneDetails(SceneNode sceneNode) {
        if (StageManager.getInstance().requestStageFocus(sceneNode.getComponentId())) return;

        //Open new window
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/SceneDetails.fxml"));
            Parent root = fxmlLoader.load();

            SceneDetails sceneDetails = fxmlLoader.getController();
            sceneDetails.initialize(sceneNode, sceneNode.getEditorWindow());

            Stage stage = new Stage();
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(sceneNode.getTitle());
            stage.setUserData(sceneNode.getComponentId());
            stage.setScene(new javafx.scene.Scene(root, 1280, 720));

            StageManager.getInstance().openStage(stage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    @Override
    protected void setupContextMenu() {
        super.setupContextMenu();

        MenuItem goodEndItem = new MenuItem("Set as Good End");
        MenuItem badEndItem = new MenuItem("Set as Bad End");
        contextMenu.getItems().addAll(2, Arrays.asList(goodEndItem, badEndItem));

        //Handle menu actions
        goodEndItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) getJOIScene().goodEndProperty().set(!isGoodEnd());
        });

        badEndItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) getJOIScene().badEndProperty().set(!isBadEnd());
        });

        setOnContextMenuRequested(contextMenuEvent -> {
            contextMenu.hide();
            if (getComponentId() == 0) {
                //Is the first scene
                contextMenu.getItems().get(contextMenu.getItems().size() - 1).setDisable(true);
                contextMenu.getItems().get(2).setDisable(true);
                contextMenu.getItems().get(3).setDisable(true);
            } else {
                contextMenu.getItems().get(contextMenu.getItems().size() - 1).setDisable(false);

                //Change name of ending buttons
                if (isBadEnd()) {
                    contextMenu.getItems().get(3).setText("Remove Ending Tag");
                    contextMenu.getItems().get(2).setDisable(true);
                } else {
                    contextMenu.getItems().get(3).setText("Set as Bad End");
                    contextMenu.getItems().get(2).setDisable(false);
                }

                if (isGoodEnd()) {
                    contextMenu.getItems().get(2).setText("Remove Ending Tag");
                    contextMenu.getItems().get(3).setDisable(true);
                } else {
                    contextMenu.getItems().get(2).setText("Set as Good End");
                    contextMenu.getItems().get(3).setDisable(false);
                }
            }
            contextMenu.show(this, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });
    }

    @Override
    public void focusState(boolean value) {
        if (value) {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        } else {
            setStyle(
                    "-fx-background-color: #5a5a5a;" +
                            "-fx-background-radius: 10;" +
                            "-fx-background-insets: 8;" +
                            "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 1);" +
                            "-fx-opacity: 1;"
            );
        }
    }

    @Override
    protected boolean openDialog() {
        openSceneDetails(this);
        return true;
    }

    //Getters and setters
    public Scene getJOIScene() {
        return (Scene) joiComponent;
    }

    public boolean isBadEnd() {
        return getJOIScene().badEndProperty().getValue();
    }

    public boolean isGoodEnd() {
        return getJOIScene().goodEndProperty().getValue();
    }
}
