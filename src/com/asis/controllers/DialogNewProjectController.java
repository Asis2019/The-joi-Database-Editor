package com.asis.controllers;

import com.asis.utilities.Alerts;
import com.asis.utilities.Config;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.File;


public class DialogNewProjectController {

    @FXML private TextField projectNameTextField, projectDirectoryTextField;

    @FXML private ComboBox<String> languagesDropDown;

    private File projectPath;
    private boolean firstLoad = false;

    public void initialize() {
        setProjectPath(Controller.getInstance().getJoiPackage().getPackageDirectory());

        projectNameTextField.setText("Untitled");
        projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+projectNameTextField.getText().trim());

        projectNameTextField.textProperty().addListener((observableValue, s, t1) -> projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+t1));

        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            for(int i=0; i<((JSONArray) data).length(); i++) {
                getLanguagesDropDown().getItems().add(((JSONArray) data).getJSONObject(i).getString("menu_name"));
            }
        }
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
            result = Controller.getInstance().processLoadProject();
        }

        if(result) {
            Stage stage = (Stage) projectNameTextField.getScene().getWindow();
            stage.close();
        }
    }

    public void actionButtonFinish() {
        if (!isFirstLoad() && Controller.getInstance().changesHaveOccurred()) {
            int choice = new Alerts().unsavedChangesDialog("New Project", "You have unsaved work, are you sure you want to continue?");
            switch (choice) {
                case 0:
                    return;

                case 1:
                    Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageCodeForName(getLanguagesDropDown().getValue()));
                    break;

                case 2:
                    Controller.getInstance().actionSaveProject();
                    Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageCodeForName(getLanguagesDropDown().getValue()));
                    break;
            }
        } else {
            Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim(), getLanguageCodeForName(getLanguagesDropDown().getValue()));
        }

        Stage stage = (Stage) projectNameTextField.getScene().getWindow();
        stage.close();
    }

    private String getLanguageCodeForName(String name) {
        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            for(int i=0; i<((JSONArray) data).length(); i++) {
                if(((JSONArray) data).getJSONObject(i).getString("menu_name").equals(name)) {
                    return ((JSONArray) data).getJSONObject(i).getString("file_code");
                }
            }
        }
        return "en";
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
    public void setLanguagesDropDown(ComboBox<String> languagesDropDown) {
        this.languagesDropDown = languagesDropDown;
    }
}
