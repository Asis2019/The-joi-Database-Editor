package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.controllers.Controller;
import com.asis.joi.JOIPackageManager;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import static com.asis.controllers.dialogs.DialogUnsavedChanges.*;
import static com.asis.utilities.AsisUtils.getLanguageValueForAlternateKey;


public class DialogNewProject {

    @FXML private TextField projectNameTextField, projectDirectoryTextField;

    @FXML private ComboBox<String> languagesDropDown;

    private File projectPath;
    private boolean firstLoad = false;

    public void initialize() {
        setProjectPath(JOIPackageManager.getInstance().getJoiPackageDirectory());

        projectNameTextField.setText("Untitled");
        projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+projectNameTextField.getText().trim());

        projectNameTextField.textProperty().addListener((observableValue, s, t1) -> projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+t1));

        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            for(int i=0; i<((JSONArray) data).length(); i++) {
                getLanguagesDropDown().getItems().add(((JSONArray) data).getJSONObject(i).getString("menu_name"));
            }
        }

        getLanguagesDropDown().getSelectionModel().select(0);
    }

    public void actionBrowsFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getProjectPath());
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            setProjectPath(file);
            projectDirectoryTextField.setText(getProjectPath().getPath()+File.separator+projectNameTextField.getText().trim());
        }
    }

    public void onActionLoadProject() {
        boolean result;
        if(!isFirstLoad()) {
            result = Controller.getInstance().actionLoadProject();
        } else {
            try {
                result = Controller.getInstance().processLoadProject();
            } catch (IOException e) {
                AsisUtils.errorDialogWindow(e);
                result = false;
            }
        }

        if(result) {
            Stage stage = (Stage) projectNameTextField.getScene().getWindow();
            stage.close();
        }
    }

    public void actionButtonFinish() {
        try {
            if (!isFirstLoad() && JOIPackageManager.getInstance().changesHaveOccurred()) {
                int choice = DialogUnsavedChanges.unsavedChangesDialog("New Project", "You have unsaved work, are you sure you want to continue?");
                switch (choice) {
                    case CHOICE_CANCEL:
                        return;

                    case CHOICE_DO_NOT_SAVE:
                        Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageValueForAlternateKey(getLanguagesDropDown().getValue(), "menu_name"));
                        break;

                    case CHOICE_SAVE:
                        Controller.getInstance().actionSaveProject();
                        Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageValueForAlternateKey(getLanguagesDropDown().getValue(), "menu_name"));
                        break;
                }
            } else {
                Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageValueForAlternateKey(getLanguagesDropDown().getValue(), "menu_name"));
            }

            Stage stage = (Stage) projectNameTextField.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            DialogMessage.messageDialog("Error", e.getMessage());
        } catch (FileAlreadyExistsException e) {
            //User canceled the creation of new joi.
        }
    }

    public static void newProjectWindow(boolean firstLoadCheck) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/resources/fxml/dialog_new_project.fxml"));
            Parent root = fxmlLoader.load();

            DialogNewProject dialogNewProject = fxmlLoader.getController();
            dialogNewProject.setFirstLoad(firstLoadCheck);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/images/icon.png")));
            stage.setTitle("New Project");
            stage.setScene(new Scene(root, 600, 400));
            stage.setOnCloseRequest(windowEvent -> {
                if (firstLoadCheck) Controller.getInstance().quiteProgram();
            });
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    //Getters and setters
    public File getProjectPath() {
        return projectPath;
    }
    public void setProjectPath(File projectPath) {
        this.projectPath = projectPath;
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }
    public void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }

    public ComboBox<String> getLanguagesDropDown() {
        return languagesDropDown;
    }

}
