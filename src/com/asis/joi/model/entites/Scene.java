package com.asis.joi.model.entites;

import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.entites.dialog.Dialog;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;
import java.util.ArrayList;

public class Scene implements JSONString, JOIEntity<JSONObject>, Cloneable {
    private int sceneId;
    private double layoutXPosition, layoutYPosition;
    private String sceneTitle;
    private boolean noFade = false;
    private File sceneImage;
    private Timer timer;
    private Dialog dialog;
    private CustomDialogueBox customDialogueBox;
    private Transition transition = new Transition();
    private GotoScene gotoScene;
    private ArrayList<Line> lineArrayList = new ArrayList<>();

    private final ReadOnlyBooleanWrapper badEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper goodEnd = new ReadOnlyBooleanWrapper();

    public Scene() {
        this(0, 0, 10, "Scene 1");
    }

    public Scene(int sceneId) {
        this(sceneId, 0, 10, "Scene " + (sceneId + 1));
    }

    public Scene(int sceneId, int layoutXPosition, int layoutYPosition, String sceneTitle) {
        setSceneId(sceneId);
        setLayoutXPosition(layoutXPosition);
        setLayoutYPosition(layoutYPosition);
        setSceneTitle(sceneTitle);
    }

    public void addNewLine(int lineNumber) {
        JSONObject lineObject = new JSONObject();
        lineObject.put("id", lineNumber);

        getLineArrayList().add(Line.createEntity(lineObject));
    }
    public void removeLine(final int lineNumber) {
        for (Line line : getLineArrayList()) {
            if (line.getLineNumber() > lineNumber) {
                line.setLineNumber(line.getLineNumber() - 1);
            }
        }

        getLineArrayList().remove(getLine(lineNumber));
    }

    public Line getLine(int lineNumber) {
        for (Line line : getLineArrayList()) {
            if (line.getLineNumber() == lineNumber) {
                return line;
            }
        }
        return null;
    }

    /**
     * Checks if scene has the entity passed in. Returns true if the passed entity is the same as the entity stored in the scene.
     *
     * @param entity - the joi entity to check for
     * @return boolean
     */
    public boolean containsEntity(JOIEntity<?> entity) {
        if (entity instanceof Timer) {
            if (getTimer() != null) return getTimer().equals(entity);
        }

        if (entity instanceof Dialog) {
            if (getDialog() != null) return getDialog().equals(entity);
        }

        if (entity instanceof Transition) {
            if (getTransition() != null) return getTransition().equals(entity);
        }

        return false;
    }

    public static Scene createEntity(JSONObject jsonObject) {
        Scene scene = new Scene();

        //Note scene id is not set here, it is set by the joi class
        //Set other fields
        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "sceneId":
                    scene.setSceneId(jsonObject.getInt("sceneId"));
                    break;
                case "sceneTitle":
                    scene.setSceneTitle(jsonObject.getString("sceneTitle"));
                    break;
                case "layoutXPosition":
                    scene.setLayoutXPosition(jsonObject.getDouble("layoutXPosition"));
                    break;
                case "layoutYPosition":
                    scene.setLayoutYPosition(jsonObject.getDouble("layoutYPosition"));
                    break;
                case "joiEnd":
                    scene.setGoodEnd(true);
                    break;
                case "badJoiEnd":
                    scene.setBadEnd(true);
                    break;
                case "sceneImage":
                    if (jsonObject.get("sceneImage") instanceof JSONObject) {
                        scene.setSceneImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getJSONObject("sceneImage").getString("name")));
                    } else {
                        scene.setSceneImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getString("sceneImage")));
                    }
                    break;
                case "noFade":
                    scene.setNoFade(jsonObject.getBoolean("noFade"));
                    break;
                case "transition":
                    scene.setTransition(Transition.createEntity(jsonObject.getJSONArray("transition").getJSONObject(0)));
                    break;
                case "timer":
                    scene.setTimer(Timer.createEntity(jsonObject.getJSONArray("timer").getJSONObject(0)));
                    break;
                case "customDialogueBox":
                    scene.setCustomDialogueBox(CustomDialogueBox.createEntity(jsonObject.getJSONObject("customDialogueBox")));
                    break;
                case "dialogChoice":
                    scene.setDialog(Dialog.createEntity(jsonObject.getJSONArray("dialogChoice").getJSONObject(0)));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt("gotoScene")}));

                    scene.setGotoScene(GotoScene.createEntity(gotoObject));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRObject = new JSONObject();
                    gotoRObject.put("array", jsonObject.getJSONArray("gotoSceneInRange"));

                    scene.setGotoScene(GotoScene.createEntity(gotoRObject));
                    break;
            }
        }

        //set lines
        int i = 0;
        while (jsonObject.has("line" + i)) {
            JSONObject lineObject = jsonObject.getJSONArray("line" + i).getJSONObject(0);
            lineObject.put("id", i);

            scene.getLineArrayList().add(Line.createEntity(lineObject));
            i++;
        }

        return scene;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject sceneObject = new JSONObject();

        sceneObject.put("sceneId", getSceneId());
        sceneObject.put("sceneTitle", getSceneTitle());
        sceneObject.put("layoutXPosition", getLayoutXPosition());
        sceneObject.put("layoutYPosition", getLayoutYPosition());

        if (isNoFade()) sceneObject.put("noFade", true);
        if (goodEndProperty().getValue()) sceneObject.put("joiEnd", true);
        if (badEndProperty().getValue()) sceneObject.put("badJoiEnd", true);
        if (getSceneImage() != null) sceneObject.put("sceneImage", getSceneImage().getName());
        if (getTimer() != null) sceneObject.put("timer", getTimer().toJSON());
        if (getDialog() != null) sceneObject.put("dialogChoice", getDialog().toJSON());
        if (getTransition() != null) sceneObject.put("transition", getTransition().toJSON());
        if (getCustomDialogueBox() != null) sceneObject.put("customDialogueBox", getCustomDialogueBox().toJSON());
        if (getGotoScene() != null) sceneObject.put(getGotoScene().getJsonKeyName(), getGotoScene().getJsonValue());

        for (int i = 0; i < getLineArrayList().size(); i++)
            sceneObject.put("line" + i, getLineArrayList().get(i).toJSON());

        return sceneObject;
    }

    @Override
    public Scene clone() throws CloneNotSupportedException {
        Scene scene = (Scene) super.clone();

        ArrayList<Line> clonedArray = new ArrayList<>();
        for (Line line: getLineArrayList()) clonedArray.add(line.clone());
        scene.setLineArrayList(clonedArray);

        scene.setBadEnd(isBadEnd());
        scene.setGoodEnd(isGoodEnd());
        scene.setNoFade(isNoFade());
        scene.setLayoutXPosition(getLayoutXPosition());
        scene.setLayoutYPosition(getLayoutYPosition());
        scene.setSceneId(getSceneId());
        scene.setSceneTitle(getSceneTitle());

        scene.setCustomDialogueBox(getCustomDialogueBox()==null?null:getCustomDialogueBox().clone());
        scene.setGotoScene(getGotoScene()==null?null:getGotoScene().clone());
        scene.setDialog(getDialog()==null?null:getDialog().clone());
        scene.setTimer(getTimer()==null?null:getTimer().clone());
        scene.setTransition(getTransition()==null?null:getTransition().clone());
        scene.setSceneImage(getSceneImage());

        return scene;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Scene)) return false;

        Scene scene = (Scene) object;

        if (getSceneId() != scene.getSceneId()) return false;
        if (Double.compare(scene.getLayoutXPosition(), getLayoutXPosition()) != 0) return false;
        if (Double.compare(scene.getLayoutYPosition(), getLayoutYPosition()) != 0) return false;
        if (isNoFade() != scene.isNoFade()) return false;
        if (getSceneTitle() != null ? !getSceneTitle().equals(scene.getSceneTitle()) : scene.getSceneTitle() != null)
            return false;
        if (getSceneImage() != null ? !getSceneImage().equals(scene.getSceneImage()) : scene.getSceneImage() != null)
            return false;
        if (getTimer() != null ? !getTimer().equals(scene.getTimer()) : scene.getTimer() != null) return false;
        if (getDialog() != null ? !getDialog().equals(scene.getDialog()) : scene.getDialog() != null) return false;
        if (getCustomDialogueBox() != null ? !getCustomDialogueBox().equals(scene.getCustomDialogueBox()) : scene.getCustomDialogueBox() != null)
            return false;
        if (getTransition() != null ? !getTransition().equals(scene.getTransition()) : scene.getTransition() != null)
            return false;
        if (getGotoScene() != null ? !getGotoScene().equals(scene.getGotoScene()) : scene.getGotoScene() != null)
            return false;
        if (!getLineArrayList().equals(scene.getLineArrayList())) return false;
        if (isBadEnd() != scene.isBadEnd()) return false;
        return isGoodEnd() == scene.isGoodEnd();
    }

    //Getters and Setters
    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public double getLayoutXPosition() {
        return layoutXPosition;
    }

    public void setLayoutXPosition(double layoutXPosition) {
        this.layoutXPosition = layoutXPosition;
    }

    public double getLayoutYPosition() {
        return layoutYPosition;
    }

    public void setLayoutYPosition(double layoutYPosition) {
        this.layoutYPosition = layoutYPosition;
    }

    public String getSceneTitle() {
        return sceneTitle;
    }

    public void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }

    public boolean isNoFade() {
        return noFade;
    }

    public void setNoFade(boolean noFade) {
        this.noFade = noFade;
    }

    public File getSceneImage() {
        return sceneImage;
    }

    public void setSceneImage(File sceneImage) {
        this.sceneImage = sceneImage;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        setNoFade(transition == null);

        this.transition = transition;
    }

    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }
    private void setLineArrayList(ArrayList<Line> lineArrayList) {
        this.lineArrayList = lineArrayList;
    }

    public GotoScene getGotoScene() {
        return gotoScene;
    }

    public void setGotoScene(GotoScene gotoScene) {
        this.gotoScene = gotoScene;
    }

    public boolean isBadEnd() {
        return badEnd.get();
    }

    public ReadOnlyBooleanWrapper badEndProperty() {
        return badEnd;
    }

    public void setBadEnd(boolean badEnd) {
        this.badEnd.set(badEnd);
    }

    public boolean isGoodEnd() {
        return goodEnd.get();
    }

    public ReadOnlyBooleanWrapper goodEndProperty() {
        return goodEnd;
    }

    public void setGoodEnd(boolean goodEnd) {
        this.goodEnd.set(goodEnd);
    }

    public CustomDialogueBox getCustomDialogueBox() {
        return customDialogueBox;
    }

    public void setCustomDialogueBox(CustomDialogueBox customDialogueBox) {
        this.customDialogueBox = customDialogueBox;
    }
}
