package asis;

import asis.json.JSONArray;
import asis.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Story {
    private static Story instance = null;

    private File workingDirectory;
    private File projectIcon;
    private ArrayList<File> imagesArray = new ArrayList<>();

    private JSONObject metaDataJson = new JSONObject();
    private JSONObject storyDataJson = new JSONObject();

    public Story() {
        //Default constructor
        instance = this;

        workingDirectory = new File(System.getProperty("user.dir")+"\\defaultWorkspace");
        boolean result = workingDirectory.mkdir();
    }

    public static Story getInstance() {
        return instance;
    }

    void setProjectDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    File getProjectDirectory() {
        return workingDirectory;
    }

    public void makeSceneGoodEnd(int sceneId) {
        addDataToScene(sceneId, "joiEnd", true);
    }

    public void makeSceneBadEnd(int sceneId) {
        addDataToScene(sceneId, "badJoiEnd", true);
    }

    void addNewScene(int sceneId, String sceneTitle) {
        if(storyDataJson.has("JOI")) {
            JSONObject object = new JSONObject();
            object.put("sceneId", sceneId);
            object.put("sceneTitle", sceneTitle);
            storyDataJson.getJSONArray("JOI").put(object);
            Controller.getInstance().setNewChanges();
        } else {
            JSONArray jsonArray = new JSONArray();
            storyDataJson.put("JOI", jsonArray);
            addNewScene(sceneId, sceneTitle);
        }
    }

    void removeScene(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                storyDataJson.getJSONArray("JOI").remove(i);
                Controller.getInstance().setNewChanges();
                break;
            }
        }
    }

    void removeTransition(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("transition");
                    Controller.getInstance().setNewChanges();
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
                    Controller.getInstance().setNewChanges();
                }
            }
        }
    }

    boolean hasNoFade(int sceneId) {
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
                    Controller.getInstance().setNewChanges();
                }
            } else {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).put("sceneId", sceneId);
                addDataToScene(sceneId, key, value);
            }
        }
    }

    String getSceneImage(int sceneId) {
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

    JSONObject getSceneObject(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                return storyDataJson.getJSONArray("JOI").getJSONObject(i);
            }
        }

        return null;
    }

    void addLine(int sceneId, int lineNumber, String text) {
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
        Controller.getInstance().setNewChanges();
    }

    void addDataToLineObject(int sceneId, int lineNumber, String key, String value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0).put(key, value);
            }
        }
        Controller.getInstance().setNewChanges();
    }

    void addDataToTransition(int sceneId, String key, Object value) {
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
        Controller.getInstance().setNewChanges();
    }

    JSONObject getTransitionData(int sceneId) {
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

    void addDataToTimerObject(int sceneId, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).put("totalTime", value);
                } else {
                    //add and transition object
                    JSONObject transitionObject = new JSONObject();

                    transitionObject.put("totalTime", value);

                    JSONArray wrapper = new JSONArray();
                    wrapper.put(transitionObject);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("timer", wrapper);
                }
            }
        }
    }

    JSONObject getTimerData(int sceneId) {
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

    void addDataToTimerLineObject(int sceneId, String lineIndex, String key, Object value) {
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
        Controller.getInstance().setNewChanges();
    }

    JSONObject getTimerLineData(int sceneId, String lineIndex) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).has(lineIndex)) {
                        return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).getJSONArray(lineIndex).getJSONObject(0);
                    }
                }
            }
        }

        return null;
    }

    void removeTimer(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("timer");
                    Controller.getInstance().setNewChanges();
                }
            }
        }
    }

    JSONObject getLineData(int sceneId, int lineNumber) {
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

    int getTotalLinesInScene(int sceneId) {
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

    void addTitleToMetadataObject(String title) {
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

    void setMetadataObject(JSONObject object) {
        this.metaDataJson = object;
    }

    JSONObject getMetadataObject() {
        return metaDataJson;
    }

    JSONObject getStoryDataJson() {
        return storyDataJson;
    }

    ArrayList<File> getImagesArray() {
        return imagesArray;
    }

    void addImage(File imageFile) {
        imagesArray.add(imageFile);
    }

    void addMetadataIcon(File iconFile) {
        this.projectIcon = iconFile;
    }

    File getMetadataIcon() {
        return projectIcon;
    }

    void removeDialog(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("dialogChoice");
                    Controller.getInstance().setNewChanges();
                }
            }
        }
    }

    void removeDialogOption(int sceneId, int optionNumber) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).has("option"+optionNumber)) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).remove("option"+optionNumber);
                        Controller.getInstance().setNewChanges();
                    }
                }
            }
        }
    }

    JSONObject getDialogData(int sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0);
                }
            }
        }

        return null;
    }

    void addDialogOptionText(int sceneId, String optionText, int optionNumber) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).has("option"+optionNumber)) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice")
                                .getJSONObject(0)
                                .getJSONArray("option"+optionNumber)
                                .getJSONObject(0).put("text", optionText);
                    } else {
                        //create dialog and rerun method
                        JSONObject object = new JSONObject();
                        JSONArray wrapper = new JSONArray();
                        wrapper.put(object);
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).put("option"+optionNumber, wrapper);
                        addDialogOptionText(sceneId, optionText, optionNumber);
                    }
                } else {
                    //create dialog and rerun method
                    JSONObject object = new JSONObject();
                    JSONArray wrapper = new JSONArray();
                    wrapper.put(object);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("dialogChoice", wrapper);
                    addDialogOptionText(sceneId, optionText, optionNumber);
                }
            }
        }
        Controller.getInstance().setNewChanges();
    }

    void addDialogOptionGoTo(int sceneId, int optionNumber, int gotoScene) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getInt("sceneId") == sceneId) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("dialogChoice")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).has("option"+optionNumber)) {
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice")
                                .getJSONObject(0)
                                .getJSONArray("option"+optionNumber)
                                .getJSONObject(0).put("gotoScene", gotoScene);
                    } else {
                        //create dialog and rerun method
                        JSONObject object = new JSONObject();
                        JSONArray wrapper = new JSONArray();
                        wrapper.put(object);
                        storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("dialogChoice").getJSONObject(0).put("option"+optionNumber, wrapper);
                        addDialogOptionGoTo(sceneId, optionNumber, gotoScene);
                    }
                } else {
                    //create dialog and rerun method
                    JSONObject object = new JSONObject();
                    JSONArray wrapper = new JSONArray();
                    wrapper.put(object);
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).put("dialogChoice", wrapper);
                    addDialogOptionGoTo(sceneId, optionNumber, gotoScene);
                }
            }
        }
        Controller.getInstance().setNewChanges();
    }

    void setStoryDataJson(JSONObject object) {
        this.storyDataJson = object;
    }
}
