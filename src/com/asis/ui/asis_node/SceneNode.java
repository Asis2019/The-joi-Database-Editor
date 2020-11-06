package com.asis.ui.asis_node;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogSceneTitle;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneImage;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.NoSuchElementException;

public class SceneNode extends JOIComponentNode {
    private final ReadOnlyBooleanWrapper isBadEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper isGoodEnd = new ReadOnlyBooleanWrapper();
    private final ImageView imageView = new ImageView();
    private final Rectangle textBackdrop = new Rectangle();

    public SceneNode(int width, int height, int sceneId, SceneNodeMainController sceneNodeMainController, Scene scene) {
        super(width, height, sceneId, sceneNodeMainController, scene);

        setUserData("sceneNode");

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

        hideSceneThumbnail();
    }

    private void initializeEndVariables() {
        isGoodEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isGoodEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                getInputConnection().setButtonColor(AsisConnectionButton.GOOD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                getInputConnection().setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isBadEndProperty().addListener((observableValue, aBoolean, t1) -> {
            if (isBadEnd()) {
                // Make children hidden
                setOutputConnectionsInvisible();
                getInputConnection().setButtonColor(AsisConnectionButton.BAD_END_COLOR);
            } else {
                // Make children visible
                setOutputConnectionsVisible();
                getInputConnection().setButtonColor(AsisConnectionButton.DEFAULT_COLOR);
            }
        });

        isGoodEndProperty().bindBidirectional(getJOIScene().goodEndProperty());
        isBadEndProperty().bindBidirectional(getJOIScene().badEndProperty());
    }

    public void showSceneThumbnail() {
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
        } catch (NoSuchElementException ignored) {
            hideSceneThumbnail();
        }
    }

    public void hideSceneThumbnail() {
        textBackdrop.setVisible(false);
        imageView.setVisible(false);
        imageView.setImage(null);
    }

    @Override
    protected void setupContextMenu() {
        Controller controller = Controller.getInstance();

        MenuItem editSceneItem = new MenuItem("Edit Scene");
        MenuItem editNameItem = new MenuItem("Change Name");
        MenuItem goodEndItem = new MenuItem("Set as Good End");
        MenuItem badEndItem = new MenuItem("Set as Bad End");
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem deleteNodeItem = new MenuItem("Delete");
        contextMenu.getItems().addAll(editSceneItem, editNameItem, goodEndItem, badEndItem, separatorMenuItem, deleteNodeItem);

        //Handle menu actions
        editSceneItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                controller.openSceneDetails(this);
            }
        });

        editNameItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                String title = DialogSceneTitle.addNewSceneDialog(getTitle());
                if(title == null) return;

                controller.getJoiPackage().getJoi().getComponent(getComponentId()).setComponentTitle(title);
                setTitle(title);
            }
        });

        deleteNodeItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                controller.removeComponentNode(this);
            }
        });

        goodEndItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                setIsGoodEnd(!isGoodEnd());
            }
        });

        badEndItem.setOnAction(actionEvent -> {
            if (getJoiComponent() != null) {
                setIsBadEnd(!isBadEnd());
            }
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
            //super.getOnContextMenuRequested().handle(contextMenuEvent);
        });
    }

    @Override
    protected void focusState(boolean value) {
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

    //Getters and setters
    public Scene getJOIScene() {
        return (Scene) joiComponent;
    }

    public boolean isBadEnd() {
        return isBadEnd.getValue();
    }

    public ReadOnlyBooleanWrapper isBadEndProperty() {
        return isBadEnd;
    }

    public void setIsBadEnd(boolean isBadEnd) {
        this.isBadEnd.set(isBadEnd);
    }

    public boolean isGoodEnd() {
        return isGoodEnd.getValue();
    }

    public ReadOnlyBooleanWrapper isGoodEndProperty() {
        return isGoodEnd;
    }

    public void setIsGoodEnd(boolean isGoodEnd) {
        this.isGoodEnd.set(isGoodEnd);
    }
}
