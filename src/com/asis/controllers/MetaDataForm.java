package com.asis.controllers;

import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.MetaData;
import com.asis.ui.NumberField;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;

import java.io.File;

public class MetaDataForm {
    private File iconFile = null;
    private final ImageView imageView = new ImageView();
    private JOIPackage joiPackage;

    @FXML private VBox mainVBox, iconControllerBox;
    @FXML private TextField titleTextField, displayedFetishesTextField,
            joiIdTextField, gameVersionTextField, creatorTextField;
    @FXML private TextArea fetishesTextArea, equipmentTextArea, charactersTextArea, franchiseTextArea, preparationsTextArea;
    @FXML private MenuButton featureSelection;
    @FXML private CheckBox customDuration;
    @FXML private NumberField estimatedDurationField;

    public void initialize() {
        getImageView().setFitHeight(300);
        getImageView().setFitWidth(300);
        getImageView().setCursor(Cursor.HAND);
        getImageView().setOnMouseClicked(mouseEvent -> addIcon());

        setIdUpdatingListeners();

        Tooltip tooltip = new Tooltip("Input the duration of your joi in seconds");
        customDuration.setTooltip(tooltip);
        estimatedDurationField.disableProperty().bind(customDuration.selectedProperty().not());
    }

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
        preparationsTextArea.setText(metaData.getPreparations().replaceAll("#","\n"));
        displayedFetishesTextField.setText(metaData.getDisplayedFetishes());
        joiIdTextField.setText(metaData.getJoiId());
        gameVersionTextField.setText(metaData.getVersionAdded());
        creatorTextField.setText(metaData.getCreator());
        fetishesTextArea.setText(String.join(",", metaData.getFetishList()));
        charactersTextArea.setText(String.join(",", metaData.getCharacterList()));
        equipmentTextArea.setText(String.join(",", metaData.getEquipmentList()));
        franchiseTextArea.setText(String.join(",", metaData.getFranchiseList()));

        if(!metaData.isUsingCustomDuration()) joiPackage.getMetaData().setEstimatedDuration(joiPackage.getJoi().getDuration());

        estimatedDurationField.setText(String.valueOf(metaData.getEstimatedDuration()));
        customDuration.setSelected(metaData.isUsingCustomDuration());


        //Populate featureSelection from file
        JSONArray availableFeatures = (JSONArray) Config.get("FEATURES");
        for(int i=0; i<availableFeatures.length(); i++) {
            CheckBox checkBox = new CheckBox(availableFeatures.getJSONObject(i).getString("feature_read_name"));
            checkBox.setTooltip(new Tooltip(availableFeatures.getJSONObject(i).getString("feature_description")));
            checkBox.prefWidthProperty().bind(featureSelection.widthProperty());
            checkBox.setTextFill(new Color(0.73, 0.73, 0.73, 1));

            CustomMenuItem customMenuItem = new CustomMenuItem(checkBox);
            customMenuItem.setHideOnClick(false);

            if(metaData.getFeatureList().contains(availableFeatures.getJSONObject(i).getString("feature_code")))
                checkBox.setSelected(true);

            featureSelection.getItems().add(customMenuItem);
        }
    }

    private void updateJoiField(final String creator, final String title) {
        String preparedCreator="", preparedTitle="";

        if(creator != null && !creator.isEmpty()) preparedCreator = creator.toLowerCase().replaceAll(" ","_").replaceAll("#", "_");
        if(title != null && !title.isEmpty()) preparedTitle = title.toLowerCase().replaceAll(" ","_").replaceAll("#", "_");

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
        Image image = IconCreator.show();
        if(image != null) {
            setIconFile(new File(JOIPackageManager.getInstance().getJoiPackageDirectory(), "joi_icon.png"));

            mainVBox.getChildren().remove(getImageView());

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
        metaData.setPreparations(preparationsTextArea.getText().trim().replaceAll("\n","#"));
        metaData.setDisplayedFetishes(displayedFetishesTextField.getText());
        metaData.setJoiId(joiIdTextField.getText());
        metaData.setVersionAdded(gameVersionTextField.getText());
        metaData.setCreator(creatorTextField.getText());

        metaData.setUsesCustomDuration(customDuration.isSelected());
        if(metaData.isUsingCustomDuration()) {
            metaData.setEstimatedDuration(Double.parseDouble(estimatedDurationField.getText().trim()));
        }

        MetaData.addCommaSeparatedStringToList(fetishesTextArea.getText().toLowerCase(), metaData.getFetishList());
        MetaData.addCommaSeparatedStringToList(equipmentTextArea.getText().toLowerCase(), metaData.getEquipmentList());
        MetaData.addCommaSeparatedStringToList(charactersTextArea.getText(), metaData.getCharacterList());
        MetaData.addCommaSeparatedStringToList(franchiseTextArea.getText(), metaData.getFranchiseList());

        JSONArray availableFeatures = (JSONArray) Config.get("FEATURES");
        featureSelection.getItems().forEach(menuItem -> {
            if(menuItem instanceof CustomMenuItem) {
                CustomMenuItem item = (CustomMenuItem) menuItem;

                CheckBox checkBox = (CheckBox) item.getContent();
                if(checkBox.isSelected()) {
                    metaData.getFeatureList().add(AsisUtils.getValueForAlternateKey(availableFeatures, checkBox.getText(), "feature_code", "feature_read_name"));
                }
            }
        });

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
