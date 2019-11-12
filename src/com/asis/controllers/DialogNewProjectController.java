package com.asis.controllers;

import com.asis.utilities.Alerts;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class DialogNewProjectController {

    @FXML private TextField projectNameTextField, projectDirectoryTextField;

    private File projectPath;
    private boolean firstLoad = false;

    public void initialize() {
        setProjectPath(Controller.getInstance().getJoiPackage().getPackageDirectory());

        projectNameTextField.setText("Untitled");
        projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+projectNameTextField.getText().trim());

        projectNameTextField.textProperty().addListener((observableValue, s, t1) -> projectDirectoryTextField.setText(getProjectPath().getAbsolutePath()+File.separator+t1));
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
                    Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim());
                    break;

                case 2:
                    Controller.getInstance().actionSaveProject();
                    Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim());
                    break;
            }
        } else {
            Controller.getInstance().processNewProject(getProjectPath(), projectNameTextField.getText().trim());
        }

        Stage stage = (Stage) projectNameTextField.getScene().getWindow();
        stage.close();
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
}
