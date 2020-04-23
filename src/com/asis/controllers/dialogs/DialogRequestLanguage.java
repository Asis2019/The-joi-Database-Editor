package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.joi.JOIPackageManager;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import javafx.event.Event;
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

//TODO requires a way for the calling class to controll the languages that display in the dropdown
public class DialogRequestLanguage {
    @FXML
    private ComboBox<String> languagesDropDown;
    private static String selectedLanguageCode;
    private boolean allowAllLanguages;

    public void populateDropdown() {
        Object data = Config.get("LANGUAGES");
        if (data instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) data).length(); i++) {
                if(!allowAllLanguages) {
                    if (JOIPackageManager.getInstance().getJoiPackageLanguages().contains(((JSONArray) data).getJSONObject(i).getString("file_code")))
                        languagesDropDown.getItems().add(((JSONArray) data).getJSONObject(i).getString("menu_name"));
                } else {
                    languagesDropDown.getItems().add(((JSONArray) data).getJSONObject(i).getString("menu_name"));
                }
            }
        }

        languagesDropDown.getSelectionModel().select(0);
    }

    public void actionContinue() {
        selectedLanguageCode = AsisUtils.getLanguageCodeForName(languagesDropDown.getValue());
        Stage stage = (Stage) languagesDropDown.getScene().getWindow();
        stage.close();
    }

    public static String requestLanguage(boolean allowAllLanguages) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Alerts.class.getResource("/resources/fxml/dialog_request_working_language.fxml"));
            Parent root = fxmlLoader.load();

            DialogRequestLanguage dialogRequestLanguage = fxmlLoader.getController();
            dialogRequestLanguage.setAllowAllLanguages(allowAllLanguages);
            dialogRequestLanguage.populateDropdown();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Chose project language");
            stage.setOnCloseRequest(Event::consume);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return selectedLanguageCode;
    }

    public void setAllowAllLanguages(boolean allowAllLanguages) {
        this.allowAllLanguages = allowAllLanguages;
    }
}
