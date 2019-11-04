package com.asis.joi;

import com.asis.joi.dialog.Dialog;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Scene {
    //TODO goto scene here and in dialog should not be an int type
    private int sceneId, gotoScene, layoutXPosition, layoutYPosition;
    private String sceneTitle;
    private boolean noFade = false;
    private File sceneImage;
    private Timer timer;
    private Dialog dialog;
    private Transition transition;
    private ArrayList<Line> lineArrayList = new ArrayList<>();

    public Scene() {
        this(0, 0, 10, "Scene 1");
    }
    public Scene(int sceneId) {
        this(sceneId, 0, 10, "Scene "+(sceneId+1));
    }
    public Scene(int sceneId, int layoutXPosition, int layoutYPosition, String sceneTitle) {
        setSceneId(sceneId);
        setLayoutXPosition(layoutXPosition);
        setLayoutYPosition(layoutYPosition);
        setSceneTitle(sceneTitle);
        //setTransition(new Transition());
    }

    public void addNewLine() {
        getLineArrayList().add(new Line());
    }
    public void removeLine(int lineIndex) {
        getLineArrayList().remove(lineIndex);
    }

    public JSONObject getSceneAsJson() {
        //Make new Object
        JSONObject sceneObject = new JSONObject();

        //Set values
        sceneObject.put("sceneId", getSceneId());
        sceneObject.put("sceneTitle", getSceneTitle());
        sceneObject.put("gotoScene", getGotoScene());
        sceneObject.put("layoutXPosition", getLayoutXPosition());
        sceneObject.put("layoutYPosition", getLayoutYPosition());
        if(isNoFade()) sceneObject.put("noFade", true);
        if(getSceneImage() != null) sceneObject.put("sceneImage", getSceneImage().getName());
        if(getTimer() != null) sceneObject.put("timer", getTimer().getTimerAsJson());
        if(getDialog() != null) sceneObject.put("dialogChoice", getDialog().getDialogAsJson());
        if(getTransition() != null) sceneObject.put("transition", getTransition().getTransitionAsJson());
        for(int i=0; i<getLineArrayList().size(); i++) {
            sceneObject.put("line"+i, getLineArrayList().get(i).getLineAsJson());
        }

        return sceneObject;
    }

    @Override
    public String toString() {
        return getSceneAsJson().toString(4);
    }

    //Getters and Setters
    public int getSceneId() {
        return sceneId;
    }
    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public int getGotoScene() {
        return gotoScene;
    }
    public void setGotoScene(int gotoScene) {
        this.gotoScene = gotoScene;
    }

    public int getLayoutXPosition() {
        return layoutXPosition;
    }
    public void setLayoutXPosition(int layoutXPosition) {
        this.layoutXPosition = layoutXPosition;
    }

    public int getLayoutYPosition() {
        return layoutYPosition;
    }
    public void setLayoutYPosition(int layoutYPosition) {
        this.layoutYPosition = layoutYPosition;
    }

    public String getSceneTitle() {
        return sceneTitle;
    }
    public void setSceneTitle(String sceneTitle) {
        this.sceneTitle = sceneTitle;
    }

    private boolean isNoFade() {
        return noFade;
    }
    private void setNoFade(boolean noFade) {
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
    public boolean setTimer(Timer timer) {
        if(getTimer() == null || timer == null) {
            this.timer = timer;
            return true;
        }
        return false;
    }

    public Dialog getDialog() {
        return dialog;
    }
    public boolean setDialog(Dialog dialog) {
        if(getDialog() == null || dialog == null) {
            this.dialog = dialog;
            return true;
        }
        return false;
    }

    public Transition getTransition() {
        return transition;
    }
    public boolean setTransition(Transition transition) {
        if(getTransition() == null || transition == null) {
            if (transition == null) setNoFade(true);
            else setNoFade(false);

            this.transition = transition;
            return true;
        }
        return false;
    }

    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }
    public void setLineArrayList(ArrayList<Line> lineArrayList) {
        this.lineArrayList = lineArrayList;
    }
}
