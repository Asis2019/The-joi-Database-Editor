package com.asis;

import com.asis.controllers.Controller;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public final class Story {
    private static Story instance = null;

    private File workingDirectory;
    private File projectIcon;
    private ArrayList<File> imagesArray = new ArrayList<>();

    private JSONObject metaDataJson = new JSONObject();
    private JSONObject storyDataJson = new JSONObject();

    public Story() {
        //Default constructor
        instance = this;

        //Create project directory if needed
        setProjectDirectory(new File(System.getProperty("user.dir")+"\\defaultWorkspace"));
        boolean result = getProjectDirectory().mkdir();
    }

    public static Story getInstance() {
        return instance;
    }

    public void setProjectDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public File getProjectDirectory() {
        return workingDirectory;
    }

    public void makeSceneGoodEnd(int sceneId) {
        addDataToScene(sceneId, "joiEnd", true);
    }

    public void makeSceneBadEnd(int sceneId) {
        addDataToScene(sceneId, "badJoiEnd", true);
    }

    public void addNewScene(int sceneId, String sceneTitle) {
        if(getStoryDataJson().has("JOI")) {
            JSONObject object = new JSONObject();
            object.put("sceneId", sceneId);
            object.put("sceneTitle", sceneTitle);
            getStoryDataJson().getJSONArray("JOI").put(object);
            Controller.getInstance().setNewChanges(true);
        } else {
            JSONArray jsonArray = new JSONArray();
            getStoryDataJson().put("JOI", jsonArray);
            addNewScene(sceneId, sceneTitle);
        }
    }

    public void removeScene(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                storyDataJson.getJSONArray("JOI").remove(i);
                Controller.getInstance().setNewChanges(true);
                break;
            }
        }
    }

    public void removeTransition(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("transition");
                    Controller.getInstance().setNewChanges(true);
                }
            }
        }
    }

    public void removeDataFromScene(int sceneId, String key) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has(key)) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove(key);
                    Controller.getInstance().setNewChanges(true);
                }
            }
        }
    }

    public boolean hasNoFade(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("noFade")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getBoolean("noFade");
                }
            }
        }

        return false;
    }

    public void addDataToScene(int sceneId, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("sceneId")) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put(key, value);
                    Controller.getInstance().setNewChanges(true);
                }
            } else {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).put("sceneId", sceneId);
                addDataToScene(sceneId, key, value);
            }
        }
    }

    public void addValueToSceneGotoRange(int sceneId, int sceneGotoId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("sceneId")) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("gotoSceneInRange")) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("gotoSceneInRange").put(sceneGotoId);
                        Controller.getInstance().setNewChanges(true);
                    } else {
                        JSONArray array = new JSONArray();
                        array.put(sceneGotoId);
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).put("gotoSceneInRange", array);
                    }
                }
            }
        }
    }

    public void removeValueFromSceneGotoRange(int sceneId, int removeIdValue) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("sceneId")) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("gotoSceneInRange")) {
                        for(int ii=0; ii < storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("gotoSceneInRange").length(); ii++) {
                            int arrayValue = storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("gotoSceneInRange").getInt(ii);
                            if(arrayValue == removeIdValue) {
                                storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("gotoSceneInRange").remove(ii);
                                Controller.getInstance().setNewChanges(true);
                            }
                        }
                    } else {
                        System.out.println("No range present");
                    }
                }
            }
        }
    }

    public String getSceneImage(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("sceneImage")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneImage");
                }
            }
        }

        return null;
    }

    public JSONObject getSceneObject(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                return storyDataJson.getJSONArray("JOI").getJSONObject(i);
            }
        }

        return null;
    }

    public void addLine(int sceneId, int lineNumber, String text) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("line"+lineNumber)) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0).put("text", text);
                } else {
                    //add and array with lineNumber as its id
                    JSONObject textObject = new JSONObject();

                    textObject.put("text", text);

                    JSONArray wrapper = new JSONArray();
                    wrapper.put(textObject);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("line" + lineNumber, wrapper);
                }
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void addDataToLineObject(int sceneId, int lineNumber, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0).put(key, value);
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void removeDataFromLineObject(int sceneId, int lineNumber, String key) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0).remove(key);
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void addDataToTransition(int sceneId, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("transition").getJSONObject(0).put(key, value);
                } else {
                    //add and transition object
                    JSONObject transitionObject = new JSONObject();

                    transitionObject.put(key, value);

                    JSONArray wrapper = new JSONArray();
                    wrapper.put(transitionObject);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("transition", wrapper);
                }
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public JSONObject getTransitionData(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("transition").getJSONObject(0);
                }
            }
        }

        return null;
    }

    public void addDataToTimerObject(int sceneId, Object value) {
        JSONObject timerObject = getTimerData(sceneId);
        if(timerObject == null || timerObject.isEmpty()) {
            //add and transition object
            JSONObject transitionObject = new JSONObject();

            transitionObject.put("totalTime", value);

            JSONArray wrapper = new JSONArray();
            wrapper.put(transitionObject);
            Objects.requireNonNull(getSceneObject(sceneId)).put("timer", wrapper);
        } else {
            timerObject.put("totalTime", value);
        }
    }

    public JSONObject getTimerData(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0);
                }
            }
        }

        return null;
    }

    public void addDataToTimerLineObject(int sceneId, String lineIndex, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).has(lineIndex)) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).getJSONArray(lineIndex).getJSONObject(0).put(key, value);
                    } else {
                        //add and transition object
                        JSONObject timerObject = new JSONObject();

                        timerObject.put(key, value);

                        JSONArray wrapper = new JSONArray();
                        wrapper.put(timerObject);
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).put(lineIndex, wrapper);
                    }
                } else {
                    //add and timer object
                    JSONObject timerObject = new JSONObject();
                    JSONArray wrapper = new JSONArray();
                    wrapper.put(timerObject);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("timer", wrapper);

                    addDataToTimerLineObject(sceneId, lineIndex, key, value);
                }
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void removeDataFromTimerLineObject(int sceneId, String lineIndex, String key) {
        JSONObject lineObject = getTimerLineData(sceneId, lineIndex);
        if(lineObject != null) {
            lineObject.remove(key);
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void removeDataFromTimer(int sceneId, String lineIndex) {
        JSONObject timerObject = getTimerData(sceneId);
        if(timerObject != null && timerObject.has(lineIndex)) {
            timerObject.remove(lineIndex);
        }
        Controller.getInstance().setNewChanges(true);
    }

    public JSONObject getTimerLineData(int sceneId, String lineIndex) {
        JSONObject timerObject = getTimerData(sceneId);
        if(timerObject != null && timerObject.has(lineIndex)) {
            return timerObject.getJSONArray(lineIndex).getJSONObject(0);
        }

        return null;
    }

    public void removeTimer(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("timer");
                    Controller.getInstance().setNewChanges(true);
                }
            }
        }
    }

    public JSONObject getLineData(int sceneId, int lineNumber) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("line"+lineNumber)) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0);
                }
            }
        }

        return null;
    }

    public int getTotalLinesInScene(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                int total = 0;
                while (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("line"+total)) {
                    total++;
                }
                return total;
            }
        }

        return 1;
    }

    public void addTitleToMetadataObject(String title) {
        if(metaDataJson.has("JOI METADATA")) {
            metaDataJson.getJSONArray("JOI METADATA").getJSONObject(0).put("name", title);
        } else {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            array.put(object);
            metaDataJson.put("JOI METADATA", array);
            addTitleToMetadataObject(title);
        }
    }

    public void setMetadataObject(JSONObject object) {
        this.metaDataJson = object;
    }

    public JSONObject getMetadataObject() {
        return metaDataJson;
    }

    public JSONObject getStoryDataJson() {
        return storyDataJson;
    }

    public ArrayList<File> getImagesArray() {
        return imagesArray;
    }

    public void addImage(File imageFile) {
        imagesArray.add(imageFile);
    }

    public void addMetadataIcon(File iconFile) {
        this.projectIcon = iconFile;
    }

    public File getMetadataIcon() {
        return projectIcon;
    }

    public void removeDialog(int sceneId) {
        for(int i=0; i < getSceneAmount(); i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("dialogChoice");
                    Controller.getInstance().setNewChanges(true);
                }
            }
        }
    }

    public void removeDialogOption(int sceneId, int optionNumber) {
        JSONObject dialogObject = getDialogData(sceneId);
        if(dialogObject != null && dialogObject.has("option"+optionNumber)) {
            dialogObject.remove("option"+optionNumber);
            Controller.getInstance().setNewChanges(true);
        }
    }

    public JSONObject getDialogData(int sceneId) {
        for(int i=0; i < getSceneAmount(); i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0);
                }
            }
        }

        return null;
    }

    public void addDialogOptionText(int sceneId, String optionText, int optionNumber) {
        JSONObject dialogObject = getDialogData(sceneId);
        if(dialogObject != null) {
            if (dialogObject.has("option" + optionNumber)) {
                dialogObject.getJSONArray("option" + optionNumber).getJSONObject(0).put("text", optionText);
            } else {
                //create dialog and rerun method
                JSONObject object = new JSONObject();
                JSONArray wrapper = new JSONArray();
                wrapper.put(object);
                dialogObject.put("option" + optionNumber, wrapper);
                addDialogOptionText(sceneId, optionText, optionNumber);
            }
        } else {
            //create dialog and rerun method
            JSONObject object = new JSONObject();
            JSONArray wrapper = new JSONArray();
            wrapper.put(object);
            Objects.requireNonNull(getSceneObject(sceneId)).put("dialogChoice", wrapper);
            addDialogOptionText(sceneId, optionText, optionNumber);
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void addDialogOptionData(int sceneId, int optionNumber, String key, Object value) {
        JSONObject dialogObject = getDialogData(sceneId);
        if(dialogObject != null) {
            if(dialogObject.has("option"+optionNumber)) {
                dialogObject.getJSONArray("option"+optionNumber).getJSONObject(0).put(key, value);
            } else {
                //create dialog and rerun method
                JSONObject object = new JSONObject();
                JSONArray wrapper = new JSONArray();
                wrapper.put(object);
                dialogObject.put("option"+optionNumber, wrapper);
                addDialogOptionData(sceneId, optionNumber, key, value);
            }
        } else {
            //create dialog and rerun method
            JSONObject object = new JSONObject();
            JSONArray wrapper = new JSONArray();
            wrapper.put(object);
            Objects.requireNonNull(getSceneObject(sceneId)).put("dialogChoice", wrapper);
            addDialogOptionData(sceneId, optionNumber, key, value);
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void removeDialogOptionData(int sceneId, int optionNumber, String key) {
        for(int i=0; i < getSceneAmount(); i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).has("option"+optionNumber)) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice")
                                .getJSONObject(0)
                                .getJSONArray("option"+optionNumber)
                                .getJSONObject(0).remove(key);
                    }
                }
            }
        }
        Controller.getInstance().setNewChanges(true);
    }

    public void addValueToDialogOptionGotoRange(int sceneId, int optionNumber, int sceneGotoId) {
        JSONObject dialogData = getDialogData(sceneId);
        if(dialogData != null && dialogData.has("option"+optionNumber)) {
            if(dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).has("gotoSceneInRange")) {
                dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).getJSONArray("gotoSceneInRange").put(sceneGotoId);
                Controller.getInstance().setNewChanges(true);
            } else {
                JSONArray array = new JSONArray();
                array.put(sceneGotoId);
                dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).put("gotoSceneInRange", array);
            }
        }
    }

    public void removeValueFromDialogOptionGotoRange(int sceneId, int optionNumber, int removeIdValue) {
        JSONObject dialogData = getDialogData(sceneId);
        if(dialogData != null && dialogData.has("option"+optionNumber)) {
            if(dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).has("gotoSceneInRange")) {
                for(int ii=0; ii < dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).getJSONArray("gotoSceneInRange").length(); ii++) {
                    int arrayValue = dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).getJSONArray("gotoSceneInRange").getInt(ii);
                    if(arrayValue == removeIdValue) {
                        dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).getJSONArray("gotoSceneInRange").remove(ii);
                        Controller.getInstance().setNewChanges(true);
                    }
                }
            } else {
                System.out.println("No range present: Dialog section");
            }
        }
    }

    public void convertValueFromDialogOptionGotoRangeToSingle(int sceneId, int optionNumber) {
        JSONObject dialogData = getDialogData(sceneId);
        if(dialogData != null && dialogData.has("option"+optionNumber)) {
            if(dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).has("gotoSceneInRange")) {
                int gotoValue = dialogData.getJSONArray("option"+optionNumber).getJSONObject(0).getJSONArray("gotoSceneInRange").getInt(0);
                addDialogOptionData(sceneId, optionNumber,"gotoScene", gotoValue);
                removeDialogOptionData(sceneId, optionNumber, "gotoSceneInRange");
            } else {
                System.out.println("No range present: Dialog section");
            }
        }
    }

    public void setStoryDataJson(JSONObject object) {
        this.storyDataJson = object;
    }

    public int getSceneAmount() {
        return getStoryDataJson().getJSONArray("JOI").length();
    }
}
