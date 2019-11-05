package com.asis;

import com.asis.controllers.Controller;
import com.asis.ui.asis_node.AsisConnectionButton;
import com.asis.ui.asis_node.SceneNode;
import com.asis.ui.asis_node.SceneNodeMainController;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.json.JSONArray;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.asis.utilities.AsisUtils.getFileExtension;

public class LoadProject {

    private int numberOfScenes;
    private AnchorPane anchorPane;
    private Story story;
    private SceneNodeMainController sceneNodeMainController;
    private JSONArray storyData;

    public LoadProject(int numberOfScenes, Story story, SceneNodeMainController sceneNodeMainController, AnchorPane anchorPane) {
        this.numberOfScenes = numberOfScenes;
        this.story = story;
        this.sceneNodeMainController = sceneNodeMainController;
        this.anchorPane = anchorPane;
    }

    private void setStoryData(JSONArray storyData) {
        this.storyData = storyData;
    }

    public boolean actionLoadProject() {
        if (Controller.getNewChanges()) {
            int choice = new Alerts().unsavedChangesDialog("Load Project", "You have unsaved work, are you sure you want to continue?");
            switch (choice) {
                case 0:
                    return false;
                case 2:
                    Controller.getInstance().actionSaveProject();
                    break;
            }
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(story.getProjectDirectory());
        File file = directoryChooser.showDialog(null);

        if(file != null) {
            boolean canContinue = false;
            //Check if project is a joi project
            canContinue = isJoiProject(file);

            if (!ifCanContinue(canContinue, file)) return false;
        } else {
            //File returned null
            return false;
        }

        //Loading completed successfully
        Controller.getInstance().setNewChanges(false);
        return true;
    }

    private boolean isJoiProject(File file) {
        for (File f : Objects.requireNonNull(file.listFiles())) {
            if (f.getName().equals("joi_text_en.json")) {
                return true;
            }
        }
        return false;
    }

    private boolean ifCanContinue(boolean canContinue, File file) {
        if (canContinue) {
            numberOfScenes = 0;
            anchorPane.getChildren().clear();
            story = new Story();

            //Set project directory to current
            story.setProjectDirectory(file);

            //Add icon to metadata and all other images to the list
            addImagesToList(file);

            //Check story for scenes and add the nodes into proper location
            if (story.getStoryDataJson().has("JOI")) {
                ArrayList<SceneNode> sceneNodes = new ArrayList<>();

                setStoryData(story.getStoryDataJson().getJSONArray("JOI"));

                if (!addNodesIntoLocations(sceneNodes)) {
                    return false;
                }

                //Start linking
                startLinking(sceneNodes);
            }
        } else {
            Alerts.messageDialog("Warning", "This is not a project folder");
            return false;
        }
        return true;
    }

    private boolean addNodesIntoLocations(ArrayList<SceneNode> sceneNodes) {
        for (int i=0; i<storyData.length(); i++) {
            int sceneId;
            String title = "Scene " + (numberOfScenes + 1);
            double xPosition = 10;
            double yPosition = 10;

            //Get scene id
            if (storyData.getJSONObject(i).has("sceneId")) {
                sceneId = storyData.getJSONObject(i).getInt("sceneId");
            } else {
                Alerts.messageDialog("Warning", "The project is not compatible with the editor");
                return false;
            }

            getSceneDetails(i, title, xPosition, yPosition);
            SceneNode sceneNode = Controller.getInstance().addScene(xPosition, yPosition, title, sceneId, true);
            //Create scene and set number of scenes
            //Set numberOfScenes to the highest id
            setSceneVariables(sceneId, i, sceneNode);
            sceneNodes.add(sceneNode);
        }
        return true;
    }

    private void setSceneVariables(int sceneId, int i, SceneNode sceneNode) {
        if(numberOfScenes <= sceneId) {
            numberOfScenes = sceneId;
        }

        //Set good ending for scene
        if (storyData.getJSONObject(i).has("joiEnd")) {
            sceneNode.setGoodEnd(true);
        }

        //Set bad ending for scene
        if (storyData.getJSONObject(i).has("badJoiEnd")) {
            sceneNode.setBadEnd(true);
        }
    }

    private void startLinking(ArrayList<SceneNode> sceneNodes) {
        for (int i=0; i<storyData.length(); i++) {
            //Check for scene normal connections
            if (storyData.getJSONObject(i).has("gotoScene")) {
                try {
                    AsisConnectionButton output = sceneNodes.get(i).getOutputButtons().get(0);
                    AsisConnectionButton input = Controller.getInstance().getSceneNodeWithId(sceneNodes, storyData.getJSONObject(i).getInt("gotoScene")).getInputConnection();
                    sceneNodeMainController.createConnection(output, input);
                } catch (NullPointerException e) {
                    System.out.println("getSceneNodeWithId(sceneNodes, sceneIdTo.get(ii)) Returned NULL");
                }
            }

            //Check for scene range connections
            sceneRangeConnections(i, sceneNodes);

            checkSceneDialogConnections(i, sceneNodes);
        }
    }

    private void sceneRangeConnections(int i, ArrayList<SceneNode> sceneNodes) {
        if (storyData.getJSONObject(i).has("gotoSceneInRange")) {
            AsisConnectionButton output = sceneNodes.get(i).getOutputButtons().get(0);

            for(int j=0; j<storyData.getJSONObject(i).getJSONArray("gotoSceneInRange").length(); j++) {
                try {
                    AsisConnectionButton input = Controller.getInstance().getSceneNodeWithId(sceneNodes, storyData.getJSONObject(i).getJSONArray("gotoSceneInRange").getInt(j)).getInputConnection();
                    sceneNodeMainController.createConnection(output, input);
                } catch (NullPointerException e) {
                    System.out.println("getSceneNodeWithId(sceneNodes, sceneIdTo.get(ii)) Returned NULL");
                }
            }
        }
    }

    private void addImagesToList(File file) {
        for (File f : Objects.requireNonNull(file.listFiles())) {
            //Check for metadata icon
            if (f.getName().equals("joi_icon.png")) {
                story.addMetadataIcon(f);
            } else if (getFileExtension(f).equals("png") || getFileExtension(f).equals("jpg") || getFileExtension(f).equals("jpeg")) {
                story.addImage(f);
            }

            //TODO for the next version we should include multi language support
            //Load metadata json
            if (f.getName().equals("info_en.json")) {
                story.setMetadataObject(AsisUtils.readJsonFromFile(f));
            }

            //Load main json
            if (f.getName().equals("joi_text_en.json")) {
                story.setStoryDataJson(AsisUtils.readJsonFromFile(f));
            }
        }
    }

    //Check for scene dialog connections
    private void checkSceneDialogConnections(int i, ArrayList<SceneNode>  sceneNodes) {
        if (storyData.getJSONObject(i).has("dialogChoice")) {
            int j = 0;
            while (storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).has("option" + j)) {
                AsisConnectionButton asisConnectionButton = sceneNodes.get(i).createNewOutputConnectionPoint("Option " + (j + 1), "dialog_option_" + (j + 1));
                asisConnectionButton.setOptionNumber(j);

                dialogOption(i, asisConnectionButton, sceneNodes, j);

                j++;
            }
        }
    }

    private void dialogOption(int i, AsisConnectionButton asisConnectionButton, ArrayList<SceneNode> sceneNodes, int j) {
        if (storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).getJSONArray("option" + j).getJSONObject(0).has("gotoScene")) {
            try {
                int gotoScene = storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).getJSONArray("option" + j).getJSONObject(0).getInt("gotoScene");
                AsisConnectionButton input = Controller.getInstance().getSceneNodeWithId(sceneNodes, gotoScene).getInputConnection();
                sceneNodeMainController.createConnection(asisConnectionButton, input);
            } catch (NullPointerException e) {
                System.out.println("getSceneNodeWithId(sceneNodes, sceneIdTo.get(ii)) Returned NULL");
            }
        } else if (storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).getJSONArray("option" + j).getJSONObject(0).has("gotoSceneInRange")){
            dialogChoice(i, j, asisConnectionButton, sceneNodes);
        }
    }

    private void dialogChoice(int i, int j, AsisConnectionButton asisConnectionButton, ArrayList<SceneNode> sceneNodes) {
        for (int jj=0; jj<storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).getJSONArray("option" + j).getJSONObject(0).getJSONArray("gotoSceneInRange").length(); jj++) {
            try {
                int gotoScene = storyData.getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).getJSONArray("option" + j).getJSONObject(0).getJSONArray("gotoSceneInRange").getInt(jj);
                AsisConnectionButton input = Controller.getInstance().getSceneNodeWithId(sceneNodes, gotoScene).getInputConnection();
                sceneNodeMainController.createConnection(asisConnectionButton, input);
            } catch (NullPointerException e) {
                System.out.println("getSceneNodeWithId(sceneNodes, sceneIdTo.get(ii)) Returned NULL");
            }
        }
    }

    private void getSceneDetails(int i, String title, double xPosition, double yPosition) {
        //Get scene title
        if (storyData.getJSONObject(i).has("sceneTitle")) {
            title = storyData.getJSONObject(i).getString("sceneTitle");
        }

        //Get scene x position
        if (storyData.getJSONObject(i).has("layoutXPosition")) {
            xPosition = storyData.getJSONObject(i).getDouble("layoutXPosition");
        }

        //Get scene y position
        if (storyData.getJSONObject(i).has("layoutYPosition")) {
            yPosition = storyData.getJSONObject(i).getDouble("layoutYPosition");
        }
    }
}
