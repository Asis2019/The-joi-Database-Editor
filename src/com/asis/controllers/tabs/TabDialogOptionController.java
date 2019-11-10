package com.asis.controllers.tabs;

import com.asis.controllers.Controller;
import com.asis.joi.components.Scene;
import com.asis.joi.components.dialog.Dialog;
import com.asis.joi.components.dialog.DialogOption;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.SceneNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TabDialogOptionController extends TabController {
    private Dialog dialog;

    @FXML private VBox buttonContainer;

    public TabDialogOptionController(String tabTitle, Dialog dialog) {
        super(tabTitle);

        setDialog(dialog);

        Platform.runLater(this::loadDialogOptionsIfPresent);
    }

    private void loadDialogOptionsIfPresent() {
        if(getDialog().getOptionArrayList() != null) {
            for(DialogOption dialogOption: getDialog().getOptionArrayList()) {
                StackPane stackPane = new StackPane();

                TextField textField = new TextField();
                textField.setMaxWidth(500);
                textField.setPromptText("Option " + (dialogOption.getOptionNumber() + 1));
                textField.setText(dialogOption.getOptionText());
                textField.setId("optionTextField");
                textField.setAlignment(Pos.CENTER);
                textField.getStylesheets().add(getClass().getResource("/resources/css/text_field_stylesheet.css").toString());

                textField.setOnKeyTyped(keyEvent -> textFieldTyped(textField, dialogOption.getOptionNumber()));

                Image image = new Image(getClass().getResource("/resources/images/dialog_option_button.png").toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(50);


                stackPane.getChildren().addAll(imageView, textField);
                buttonContainer.getChildren().add(stackPane);
            }
        }
    }

    @FXML private void actionAddOption() {
        final int totalOptions = getDialog().getOptionArrayList().size();

        if(getDialog().getOptionArrayList().size() < 4) {
            StackPane stackPane = new StackPane();

            TextField textField = new TextField();
            textField.setMaxWidth(500);
            textField.setPromptText("Option "+(totalOptions+1));
            textField.setId("optionTextField");
            textField.setAlignment(Pos.CENTER);
            textField.getStylesheets().add(getClass().getResource("/resources/css/text_field_stylesheet.css").toString());

            textField.setOnKeyTyped(keyEvent -> textFieldTyped(textField, totalOptions));

            Image image = new Image(getClass().getResource("/resources/images/dialog_option_button.png").toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(50);

            stackPane.getChildren().addAll(imageView, textField);
            buttonContainer.getChildren().add(stackPane);

            //Add new connection point to scene
            Scene scene = getScene();

            if(scene != null) {
                SceneNode sceneNode = Controller.getInstance().getSceneNodeWithId(Controller.getInstance().getSceneNodes(), scene.getSceneId());
                AsisConnectionButton asisConnectionButton = sceneNode.createNewOutputConnectionPoint("Option " + totalOptions, "dialog_option_" + totalOptions);
                asisConnectionButton.setOptionNumber(totalOptions-1);
            }

            getDialog().addDialogOption();
        }
    }

    @FXML private void actionRemoveOption() {
        final int totalOptions = getDialog().getOptionArrayList().size();

        if(totalOptions > 0) {
            buttonContainer.getChildren().remove(totalOptions-1);
            Scene scene = getScene();

            if(scene != null) {
                SceneNode sceneNode = Controller.getInstance().getSceneNodeWithId(Controller.getInstance().getSceneNodes(), scene.getSceneId());
                sceneNode.removeOutputConnection();
            }

            getDialog().removeDialogOption(totalOptions-1);
        }
    }

    private Scene getScene() {
        for(Scene scene: Controller.getInstance().getJoiPackage().getJoi().getSceneArrayList()) {
            if(scene.getDialog() != null && scene.getDialog().equals(getDialog())) {
                return scene;
            }
        }
        return null;
    }

    private void textFieldTyped(TextField textField, int optionNumber) {
        getDialog().getOptionArrayList().get(optionNumber).setOptionText(textField.getText().trim());
    }

    //Getters and setters
    public Dialog getDialog() {
        return dialog;
    }
    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
}
