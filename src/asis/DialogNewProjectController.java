package asis;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class DialogNewProjectController {

    @FXML private TextField projectNameTextField, projectDirectoryTextField;

    private File projectPath = Story.getInstance().getProjectDirectory();

    public void initialize() {
        projectNameTextField.setText("Untitled");
        projectDirectoryTextField.setText(projectPath.getPath()+"\\Untitled");

        projectNameTextField.setOnKeyTyped(keyEvent -> {
            projectDirectoryTextField.setText(projectPath.getPath()+"\\"+projectNameTextField.getText().trim());
        });

        /*projectDirectoryTextField.setOnKeyTyped(keyEvent -> {
            projectDirectoryTextField.setText(projectPath.getPath()+"\\"+projectNameTextField.getText().trim());
        });*/
    }

    public void actionBrowsFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Story.getInstance().getProjectDirectory());
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            projectPath = file;
            projectDirectoryTextField.setText(projectPath.getPath()+"\\"+projectNameTextField.getText().trim());
        }
    }

    public void onActionLoadProject() {
        Controller.getInstance().actionLoadProject();
    }

    public void actionButtonFinish() {
        if (Controller.getInstance().getNewChanges()) {
            int choice = new Alerts().unsavedChangesDialog(this.getClass(), "New Project", "You have unsaved work, are you sure you want to continue?");
            switch (choice) {
                case 0:
                    return;

                case 1:
                    Controller.createNewProject = true;
                    Controller.newProjectFile = projectPath;
                    Controller.newProjectName = projectNameTextField.getText().trim();
                    break;

                case 2:
                    Controller.getInstance().actionSaveProject();
                    Controller.createNewProject = true;
                    Controller.newProjectFile = projectPath;
                    Controller.newProjectName = projectNameTextField.getText().trim();
                    break;
            }
        } else {
            Controller.createNewProject = true;
            Controller.newProjectFile = projectPath;
            Controller.newProjectName = projectNameTextField.getText().trim();
        }

        Stage stage = (Stage) projectNameTextField.getScene().getWindow();
        stage.close();
    }
}
