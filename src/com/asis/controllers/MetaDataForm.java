package com.asis.controllers;

import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.MetaData;
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
    private final ImageView imageView = new ImageView();
    private JOIPackage joiPackage;

    @FXML private VBox mainVBox, iconControllerBox;
    @FXML private TextField titleTextField, preparationsTextField, displayedFetishesTextField,
            joiIdTextField, gameVersionTextField, creatorTextField, estimatedDurationField;
    @FXML private TextArea fetishesTextArea, equipmentTextArea, charactersTextArea, franchiseTextArea;

    public void initialize() {
        getImageView().setFitHeight(300);
        getImageView().setFitWidth(300);
        getImageView().setCursor(Cursor.HAND);
        getImageView().setOnMouseClicked(mouseEvent -> addIcon());

        setIdUpdatingListeners();
    }

    void inflateJOIPackageObject(JOIPackage joiPackage) {
        joiPackage.getMetaData().setEstimatedDuration(joiPackage.getJoi().getDuration());
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
        creatorTextField.setText(metaData.getCreator());
        fetishesTextArea.setText(String.join(",", metaData.getFetishList()));
        charactersTextArea.setText(String.join(",", metaData.getCharacterList()));
        equipmentTextArea.setText(String.join(",", metaData.getEquipmentList()));
        franchiseTextArea.setText(String.join(",", metaData.getFranchiseList()));
        estimatedDurationField.setText(String.format( "%.0f seconds",metaData.getEstimatedDuration()));
    }

    private void updateJoiField(final String creator, final String title) {
        String preparedCreator="", preparedTitle="";

        if(creator != null && !creator.isEmpty()) preparedCreator = creator.toLowerCase().replaceAll(" ","_");
        if(title != null && !title.isEmpty()) preparedTitle = title.toLowerCase().replaceAll(" ","_");

        joiIdTextField.clear();

        if(preparedCreator.equals("") && preparedTitle.equals("")) joiIdTextField.setText(null);
        else joiIdTextField.setText(preparedCreator+"_"+preparedTitle);
    }

    private void setIdUpdatingListeners() {
        titleTextField.textProperty().addListener((observableValue, s, t1) -> updateJoiField(creatorTextField.getText(), t1));

        creatorTextField.textProperty().addListener((observableValue, s, t1) -> updateJoiField(t1, titleTextField.getText()));
    }

    boolean changesHaveOccurred() {
        MetaData currentData = addFieldDataToMetaData();
        return !getJoiPackage().getMetaData().equals(currentData);
    }

    public void addIcon() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Icon");
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png", "*.png"));
        File file = fileChooser.showOpenDialog(new Stage());

        if(file != null) {
            //Image file to memory
            setIconFile(file);

            mainVBox.getChildren().remove(getImageView());

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
        metaData.setCreator(creatorTextField.getText());

        MetaData.addCommaSeparatedStringToList(fetishesTextArea.getText().toLowerCase(), metaData.getFetishList());
        MetaData.addCommaSeparatedStringToList(equipmentTextArea.getText().toLowerCase(), metaData.getEquipmentList());
        MetaData.addCommaSeparatedStringToList(charactersTextArea.getText(), metaData.getCharacterList());
        MetaData.addCommaSeparatedStringToList(franchiseTextArea.getText(), metaData.getFranchiseList());

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
}
