package com.asis.controllers.tabs;

import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.JOIComponentNode;
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

    @FXML
    private VBox buttonContainer;

    public TabDialogOptionController(String tabTitle, Dialog dialog) {
        super(tabTitle);

        setDialog(dialog);

        Platform.runLater(this::loadDialogOptionsIfPresent);
    }

    private void loadDialogOptionsIfPresent() {
        for (DialogOption dialogOption : getDialog().getOptionArrayList())
            createDialogButton(dialogOption.getOptionNumber(), dialogOption.getOptionText());
    }

    private void createDialogButton(int optionNumber, String text) {
        StackPane stackPane = new StackPane();

        TextField textField = new TextField();
        textField.setMaxWidth(500);
        textField.setPromptText("Option " + optionNumber);
        textField.setText(text);
        textField.setId("optionTextField");
        textField.setAlignment(Pos.CENTER);
        textField.getStylesheets().add(getClass().getResource("/css/text_field_stylesheet.css").toString());

        textField.textProperty().addListener((observableValue, s, t1) -> textFieldTyped(textField, optionNumber));

        Image image = new Image(getClass().getResource("/images/dialog_option_button.png").toString());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(50);


        stackPane.getChildren().addAll(imageView, textField);
        buttonContainer.getChildren().add(stackPane);
    }

    @FXML
    public void actionAddOption() {
        final int totalOptions = getDialog().getOptionArrayList().size();

        if (totalOptions < 4) {
            getDialog().addDialogOption();

            createDialogButton(totalOptions, null);

            //Add new connection point to scene
            Scene scene = getScene(getDialog());

            if (scene != null) {
                JOIComponentNode componentNode = getEditorWindow().getNodeManager().getJOIComponentNodeWithId(scene.getComponentId());
                AsisConnectionButton asisConnectionButton = componentNode.createNewOutputConnectionPoint("Option " + totalOptions, "dialog_option_" + totalOptions);
                asisConnectionButton.setOptionNumber(totalOptions);
            }
        }
    }

    @FXML
    public void actionRemoveOption() {
        final int totalOptions = getDialog().getOptionArrayList().size();

        if (totalOptions > 0) {
            buttonContainer.getChildren().remove(totalOptions - 1);
            Scene scene = getScene(getDialog());

            if (scene != null) {
                JOIComponentNode componentNode = getEditorWindow().getNodeManager().getJOIComponentNodeWithId(scene.getComponentId());
                componentNode.removeOutputConnection();
            }

            getDialog().getOptionArrayList().remove(totalOptions - 1);
        }
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
