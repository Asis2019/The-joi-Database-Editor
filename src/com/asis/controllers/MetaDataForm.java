package com.asis.controllers;

import com.asis.joi.JOIPackage;
import com.asis.joi.MetaData;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class MetaDataForm {
    private File iconFile = null;
    private ImageView imageView = new ImageView();
    private JOIPackage joiPackage;

    @FXML private VBox mainVBox, iconControllerBox;
    @FXML private TextField titleTextField, preparationsTextField, displayedFetishesTextField, joiIdTextField, gameVersionTextField;
    @FXML private TextArea fetishesTextArea, equipmentTextArea, charactersTextArea;

    void inflateJOIPackageObject(JOIPackage joiPackage) {
        setJoiPackage(joiPackage);

        //Image
        if(getJoiPackage().getMetaData().getJoiIcon() != null) {
            setIconFile(getJoiPackage().getMetaData().getJoiIcon());
            Image image = new Image(getIconFile().toURI().toString());
            getImageView().setImage(image);
            if (iconControllerBox != null) {
                mainVBox.getChildren().remove(iconControllerBox);
            }
            mainVBox.getChildren().add(0, getImageView());
        }

        MetaData metaData = getJoiPackage().getMetaData();

        //Populate all fields
        titleTextField.setText(metaData.getName());
        preparationsTextField.setText(metaData.getPreparations());
        displayedFetishesTextField.setText(metaData.getDisplayedFetishes());
        joiIdTextField.setText(metaData.getJoiId());
        gameVersionTextField.setText(metaData.getVersionAdded());
        fetishesTextArea.setText(String.join(",", metaData.getFetishList()));
        charactersTextArea.setText(String.join(",", metaData.getCharacterList()));
        equipmentTextArea.setText(String.join(",", metaData.getEquipmentList()));
    }

    public void initialize() {
        getImageView().setFitHeight(300);
        getImageView().setFitWidth(300);
        getImageView().setCursor(Cursor.HAND);
        getImageView().setOnMouseClicked(mouseEvent -> addIcon());
    }

    boolean changesHaveOccurred() {
        MetaData currentData = addFieldDataToMetaData();
        return !getJoiPackage().getMetaData().equals(currentData);
    }

    public void addIcon() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Icon");
        fileChooser.setInitialDirectory(getJoiPackage().getPackageDirectory());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png", "*.png"));
        File file = fileChooser.showOpenDialog(new Stage());

        if(file != null) {
            //Image file to memory
            setIconFile(file);

            if(getImageView() != null) {
                mainVBox.getChildren().remove(getImageView());
            }

            Image image = new Image(file.toURI().toString());
            getImageView().setImage(image);

            if(iconControllerBox != null) {
                mainVBox.getChildren().remove(iconControllerBox);
            }

            mainVBox.getChildren().add(0, getImageView());
        }
    }

    public void actionSaveButton() {
        //Process data from fields
        getJoiPackage().setMetaData(addFieldDataToMetaData());

        //Close window
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private MetaData addFieldDataToMetaData() {
        MetaData metaData = new MetaData();

        metaData.setJoiIcon(getIconFile());

        metaData.setName(titleTextField.getText());
        metaData.setPreparations(preparationsTextField.getText());
        metaData.setDisplayedFetishes(displayedFetishesTextField.getText());
        metaData.setJoiId(joiIdTextField.getText());
        metaData.setVersionAdded(gameVersionTextField.getText());

        MetaData.addCommaSeparatedStringToList(fetishesTextArea.getText(), metaData.getFetishList());
        MetaData.addCommaSeparatedStringToList(equipmentTextArea.getText(), metaData.getEquipmentList());
        MetaData.addCommaSeparatedStringToList(charactersTextArea.getText(), metaData.getCharacterList());

        return metaData;
    }

    //Getters and setters
    private JOIPackage getJoiPackage() {
        return joiPackage;
    }
    private void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }

    private File getIconFile() {
        return iconFile;
    }
    private void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    private ImageView getImageView() {
        return imageView;
    }
    private void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
