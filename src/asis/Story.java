package asis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

class Story {
    private File workingDirectory = new File(System.getProperty("user.dir"));
    private File projectIcon;
    private ArrayList<File> imagesArray = new ArrayList<>();

    private JSONObject metaDataJson = new JSONObject();
    private JSONObject storyDataJson = new JSONObject();
    private JSONArray sceneArray = new JSONArray();

    Story() {
        //Default constructor
    }

    void setProjectDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    File getProjectDirectory() {
        return workingDirectory;
    }

    void addNewScene(String sceneId) {
        JSONObject object = new JSONObject();
        object.put("sceneId", sceneId);
        sceneArray.put(object);
        storyDataJson.put("JOI", sceneArray);
    }

    void removeScene(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                storyDataJson.getJSONArray("JOI").remove(i);
            }
        }
    }

    void removeTransition(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("transition");
                }
            }
        }
    }

    void removeNoFade(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("noFade")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("noFade");
                }
            }
        }
    }

    boolean hasNoFade(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("noFade")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getBoolean("noFade");
                }
            }
        }

        return false;
    }

    void addDataToScene(String sceneId, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).put(key, value);
            }
        }
    }

    String getSceneImage(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("sceneImage")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneImage");
                }
            }
        }

        return null;
    }

    void addLine(String sceneId, int lineNumber, String text) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
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
    }

    void addDataToLineObject(String sceneId, int lineNumber, String key, String value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0).put(key, value);
            }
        }
    }

    void addDataToTransition(String sceneId, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
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
    }

    JSONObject getTransitionData(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("transition")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("transition").getJSONObject(0);
                }
            }
        }

        return null;
    }

    void addDataToTimerObject(String sceneId, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
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

    JSONObject getTimerData(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0);
                }
            }
        }

        return null;
    }

    void addDataToTimerLineObject(String sceneId, String lineIndex, String key, Object value) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
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
    }

    JSONObject getTimerLineData(String sceneId, String lineIndex) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).has(lineIndex)) {
                        return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("timer").getJSONObject(0).getJSONArray(lineIndex).getJSONObject(0);
                    }
                }
            }
        }

        return null;
    }

    void removeTimer(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if(storyDataJson.getJSONArray("JOI").getJSONObject(i).has("timer")) {
                    storyDataJson.getJSONArray("JOI").getJSONObject(i).remove("timer");
                }
            }
        }
    }

    JSONObject getLineData(String sceneId, int lineNumber) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                if (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("line"+lineNumber)) {
                    return storyDataJson.getJSONArray("JOI").getJSONObject(i).getJSONArray("line"+lineNumber).getJSONObject(0);
                }
            }
        }

        return null;
    }

    int getTotalLinesInScene(String sceneId) {
        int amountOfScenes = storyDataJson.getJSONArray("JOI").length();
        for(int i=0; i < amountOfScenes; i++) {
            if(storyDataJson.getJSONArray("JOI").getJSONObject(i).getString("sceneId").equals(sceneId)) {
                int total = 0;
                while (storyDataJson.getJSONArray("JOI").getJSONObject(i).has("line"+total)) {
                    total++;
                }
                return total+1;
            }
        }

        return 1;
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
}
