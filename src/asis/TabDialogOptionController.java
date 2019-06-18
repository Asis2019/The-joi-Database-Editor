package asis;

import asis.custom_objects.asis_node.AsisConnectionButton;
import asis.custom_objects.asis_node.SceneNode;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class TabDialogOptionController {

    private Story story;
    private SceneNode sceneNode;
    private int sceneId;
    private int totalOptions = 0;

    @FXML private VBox buttonContainer;

    public void initialize() {
    }

    void passData(Story story, SceneNode sceneNode) {
        this.story = story;
        this.sceneNode = sceneNode;
        this.sceneId = sceneNode.getSceneId();

        if(story != null) {
            initializeData();
        }
    }

    private void initializeData() {
        JSONObject jsonObject = story.getDialogData(sceneId);
        if(jsonObject != null) {
            int i =0;
            while(jsonObject.has("option"+i)) {
                StackPane stackPane = new StackPane();

                TextField textField = new TextField();
                textField.setMaxWidth(500);
                textField.setPromptText("Option " + (totalOptions + 1));
                textField.setText(jsonObject.getJSONArray("option"+i).getJSONObject(0).getString("text"));
                textField.setId("optionTextField");
                textField.setAlignment(Pos.CENTER);
                textField.getStylesheets().add(getClass().getResource("css/text_field_stylesheet.css").toString());

                int temp = totalOptions;
                textField.setOnKeyTyped(keyEvent -> textFieldTyped(textField, temp));

                Image image = new Image(getClass().getResource("images/dialog_option_button.png").toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(50);


                stackPane.getChildren().addAll(imageView, textField);
                buttonContainer.getChildren().add(stackPane);

                totalOptions++;
                i++;
            }
        }
    }

    public void actionAddOption() {
        if(totalOptions < 4) {
            StackPane stackPane = new StackPane();

            TextField textField = new TextField();
            textField.setMaxWidth(500);
            textField.setPromptText("Option "+(totalOptions+1));
            textField.setId("optionTextField");
            textField.setAlignment(Pos.CENTER);
            textField.getStylesheets().add(getClass().getResource("css/text_field_stylesheet.css").toString());

            int temp = totalOptions;
            textField.setOnKeyTyped(keyEvent -> textFieldTyped(textField, temp));

            Image image = new Image(getClass().getResource("images/dialog_option_button.png").toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(50);


            stackPane.getChildren().addAll(imageView, textField);
            buttonContainer.getChildren().add(stackPane);

            //Init values to prevent bugs
            story.addDialogOptionText(sceneId, "", totalOptions);

            totalOptions++;

            //Add new connection point to scene
            AsisConnectionButton asisConnectionButton = sceneNode.createNewOutputConnectionPoint("Option "+totalOptions, "dialog_option_"+totalOptions);
            asisConnectionButton.setOptionNumber(totalOptions-1);
        }
    }

    public void actionRemoveOption() {
        if(totalOptions > 0) {
            buttonContainer.getChildren().remove(totalOptions-1);
            sceneNode.removeOutputConnection();
            story.removeDialogOption(sceneId, totalOptions-1);
            totalOptions--;
        }
    }

    private void textFieldTyped(TextField textField, int optionNumber) {
        if(story != null) {
            story.addDialogOptionText(sceneId, textField.getText().trim(), optionNumber);
        } else {
            System.out.println("Story is null");
        }
    }
}
