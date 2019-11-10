package com.asis.controllers;

import com.asis.joi.JOIPackage;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.asis.utilities.AsisUtils.errorDialogWindow;

public class MetaDataForm {
    private File iconFile = null;
    private ImageView imageView = new ImageView();
    private boolean hasChanged = false;
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

        JSONObject metadataObject = getJoiPackage().getMetaData().getMetaDataAsJson();

        if(metadataObject.has("JOI METADATA")) {
            metadataObject = metadataObject.getJSONArray("JOI METADATA").getJSONObject(0);

            //Populate all fields
            if(metadataObject.has("name")) {
                titleTextField.setText(metadataObject.getString("name"));
            }

            if(metadataObject.has("preparations")) {
                preparationsTextField.setText(metadataObject.getString("preparations"));
            }

            if(metadataObject.has("displayedFetishes")) {
                displayedFetishesTextField.setText(metadataObject.getString("displayedFetishes"));
            }

            if(metadataObject.has("joiId")) {
                joiIdTextField.setText(metadataObject.getString("joiId"));
            }

            if(metadataObject.has("versionAdded")) {
                gameVersionTextField.setText(metadataObject.getString("versionAdded"));
            }

            if(metadataObject.has("fetish0")) {
                StringBuilder fetishString = new StringBuilder();
                int i = 0;
                while (metadataObject.has("fetish"+i)) {
                    if(metadataObject.has("fetish"+(i+1))) {
                        fetishString.append(metadataObject.getString("fetish" + i)).append(", ");
                    } else {
                        fetishString.append(metadataObject.getString("fetish" + i));
                    }
                    i++;
                }

                if(!fetishString.toString().isEmpty()) {
                    fetishesTextArea.setText(fetishString.toString());
                }
            }

            if(metadataObject.has("character0")) {
                StringBuilder characterString = new StringBuilder();
                int i = 0;
                while (metadataObject.has("character"+i)) {
                    if(metadataObject.has("character"+(i+1))) {
                        characterString.append(metadataObject.getString("character" + i)).append(", ");
                    } else {
                        characterString.append(metadataObject.getString("character" + i));
                    }
                    i++;
                }

                if(!characterString.toString().isEmpty()) {
                    charactersTextArea.setText(characterString.toString());
                }
            }

            if(metadataObject.has("toy0")) {
                StringBuilder equipmentString = new StringBuilder();
                int i = 0;
                while (metadataObject.has("toy"+i)) {
                    if(metadataObject.has("toy"+(i+1))) {
                        equipmentString.append(metadataObject.getString("toy" + i)).append(", ");
                    } else {
                        equipmentString.append(metadataObject.getString("toy" + i));
                    }
                    i++;
                }

                if(!equipmentString.toString().isEmpty()) {
                    equipmentTextArea.setText(equipmentString.toString());
                }
            }
        }
    }

    public void initialize() {
        getImageView().setFitHeight(300);
        getImageView().setFitWidth(300);
        getImageView().setCursor(Cursor.HAND);
        getImageView().setOnMouseClicked(mouseEvent -> addIcon());

        setListeners();
    }

    private void setListeners() {
        titleTextField.setOnKeyTyped(keyEvent -> setHasChanged(true));
        preparationsTextField.setOnKeyTyped(keyEvent -> setHasChanged(true));
        displayedFetishesTextField.setOnKeyTyped(keyEvent -> setHasChanged(true));
        joiIdTextField.setOnKeyTyped(keyEvent -> setHasChanged(true));
        fetishesTextArea.setOnKeyTyped(keyEvent -> setHasChanged(true));
        equipmentTextArea.setOnKeyTyped(keyEvent -> setHasChanged(true));
        charactersTextArea.setOnKeyTyped(keyEvent -> setHasChanged(true));
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
        //Create json object for metadata then set story.metadata to this object

        try {
            JSONObject innerMetadataObject = new JSONObject();
            //Single lined info
            innerMetadataObject.put("name", titleTextField.getText().trim());
            innerMetadataObject.put("preparations", preparationsTextField.getText().trim());
            innerMetadataObject.put("displayedFetishes", displayedFetishesTextField.getText().trim());
            innerMetadataObject.put("joiId", joiIdTextField.getText().trim());
            innerMetadataObject.put("versionAdded", gameVersionTextField.getText().trim());

            //Split by comma data
            String[] fetishesArray = fetishesTextArea.getText().trim().split("\\s*,\\s*");
            for(int i=0; i < fetishesArray.length; i++) {
                innerMetadataObject.put("fetish"+i, fetishesArray[i]);
            }

            String[] equipmentArray = equipmentTextArea.getText().trim().split("\\s*,\\s*");
            for(int i=0; i < equipmentArray.length; i++) {
                innerMetadataObject.put("toy"+i, equipmentArray[i]);
            }

            String[] charactersArray = charactersTextArea.getText().trim().split("\\s*,\\s*");
            for(int i=0; i < charactersArray.length; i++) {
                innerMetadataObject.put("character"+i, charactersArray[i]);
            }

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(innerMetadataObject);

            JSONObject metadataObject = new JSONObject();
            metadataObject.put("JOI METADATA", jsonArray);

            getJoiPackage().getMetaData().setJoiIcon(getIconFile());
            getJoiPackage().getMetaData().setDataFromJson(metadataObject, getJoiPackage().getPackageDirectory());

            if (!titleTextField.getText().equals("") || !preparationsTextField.getText().equals("")|| !displayedFetishesTextField.getText().equals("") || iconFile != null
                    || !joiIdTextField.getText().equals("") || !fetishesArray[0].equals("") || !equipmentArray[0].equals("") || !charactersArray[0].equals("")) {
                Controller.getInstance().setNewChanges();
            }

            //Close window
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            stage.close();
        } catch (JSONException e) {
            errorDialogWindow(e);
        }
    }

    //Getters and setters
    private JOIPackage getJoiPackage() {
        return joiPackage;
    }
    private void setJoiPackage(JOIPackage joiPackage) {
        this.joiPackage = joiPackage;
    }

    public File getIconFile() {
        return iconFile;
    }
    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public ImageView getImageView() {
        return imageView;
    }
    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }
    Boolean hasChanged() {
        return hasChanged;
    }
}
