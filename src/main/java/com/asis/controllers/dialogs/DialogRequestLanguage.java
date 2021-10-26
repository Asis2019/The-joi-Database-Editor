package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

public class DialogRequestLanguage {
    @FXML
    private ComboBox<String> languagesDropDown;

    private ArrayList<String> languages;

    private static String selectedLanguageCode;


    public void populateDropdown() {
        JSONArray array = (JSONArray) Config.get("LANGUAGES");
        for(String s: languages) languagesDropDown.getItems().add(AsisUtils.getValueForAlternateKey(array,s, "menu_name","file_code"));

        languagesDropDown.getSelectionModel().select(0);
    }

    public void actionContinue() {
        JSONArray array = (JSONArray) Config.get("LANGUAGES");
        selectedLanguageCode = AsisUtils.getValueForAlternateKey(array,languagesDropDown.getValue(), "file_code","menu_name");
        Stage stage = (Stage) languagesDropDown.getScene().getWindow();
        stage.close();
    }

    public static String requestLanguage(ArrayList<String> languages) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/dialog_request_working_language.fxml"));
            Parent root = fxmlLoader.load();

            DialogRequestLanguage dialogRequestLanguage = fxmlLoader.getController();
            dialogRequestLanguage.setLanguages(languages);
            dialogRequestLanguage.populateDropdown();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Chose project language");
            stage.setOnCloseRequest(windowEvent -> selectedLanguageCode=null);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return selectedLanguageCode;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }
}
