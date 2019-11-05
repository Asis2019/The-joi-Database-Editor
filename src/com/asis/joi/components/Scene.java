package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import com.asis.joi.components.dialog.Dialog;
import com.asis.utilities.AsisUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Scene implements JOISystemInterface {
    private int sceneId;
    private double layoutXPosition, layoutYPosition;
    private String sceneTitle;
    private boolean noFade=false, badEnding=false, goodEnding=false;
    private File sceneImage;
    private Timer timer;
    private Dialog dialog;
    private Transition transition;
    private ArrayList<Integer> gotoSceneArrayList = new ArrayList<>();
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
        sceneObject.put("layoutXPosition", getLayoutXPosition());
        sceneObject.put("layoutYPosition", getLayoutYPosition());

        if(!getGotoSceneArrayList().isEmpty()) sceneObject.put("gotoScene", getGotoSceneArrayList().toArray());
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
    public void setDataFromJson(JSONObject object) {
        //Set scene id
        if (object.has("sceneId")) {
            setSceneId(object.getInt("sceneId"));
        } else {
            //Throw scene id error
            throw new RuntimeException("Scene id was not present for one or more of the scenes.");
        }

        //Set scene title
        if (object.has("sceneTitle")) {
            setSceneTitle(object.getString("sceneTitle"));
        }

        //Set scene x position
        if (object.has("layoutXPosition")) {
            setLayoutXPosition(object.getDouble("layoutXPosition"));
        }

        //Set scene y position
        if (object.has("layoutYPosition")) {
            setLayoutYPosition(object.getDouble("layoutYPosition"));
        }

        //Set good ending for scene
        if (object.has("joiEnd")) {
            setGoodEnding(true);
        }

        //Set bad ending for scene
        if (object.has("badJoiEnd")) {
            setBadEnding(true);
        }

        //Set scene image
        if (object.has("sceneImage")) {
            setSceneImage(new File(object.getString("sceneId")));
        }

        //Set noFade
        if (object.has("noFade")) {
            setNoFade(object.getBoolean("noFade"));
        }

        //Set transition
        if (object.has("transition")) {
            setTransition(new Transition());
            getTransition().setDataFromJson(object.getJSONArray("transition").getJSONObject(0));
        }

        //Set timer
        if (object.has("timer")) {
            setTimer(new Timer());
            getTimer().setDataFromJson(object.getJSONArray("timer").getJSONObject(0));
        }

        //Set dialog
        if (object.has("dialog")) {
            setDialog(new Dialog());
            getDialog().setDataFromJson(object.getJSONArray("dialog").getJSONObject(0));
        }

        //set lines
        int i=0;
        while(object.has("line"+i)) {
            addNewLine();
            getLineArrayList().get(i).setDataFromJson(object.getJSONArray("line"+i).getJSONObject(0));
            i++;
        }

        //set gotoScene
        if (object.has("gotoScene")) {
            setGotoSceneArrayList(AsisUtils.convertJSONArrayToList(object.getJSONArray("gotoScene")));
        }
    }

    @Override
    public String toString() {
        return getSceneAsJson().toString(4);
    }

    private static boolean nullCheck(Object typeCheck, Object valueCheck) {
        return typeCheck == null || valueCheck == null;
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

    private boolean isNoFade() {
        return noFade;
    }
    private void setNoFade(boolean noFade) {
        this.noFade = noFade;
    }

    public boolean isBadEnding() {
        return badEnding;
    }
    public void setBadEnding(boolean badEnding) {
        this.badEnding = badEnding;
    }

    public boolean isGoodEnding() {
        return goodEnding;
    }
    public void setGoodEnding(boolean goodEnding) {
        this.goodEnding = goodEnding;
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
        if(nullCheck(getTimer(), timer)) {
            this.timer = timer;
            return true;
        }
        return false;
    }

    public Dialog getDialog() {
        return dialog;
    }
    public boolean setDialog(Dialog dialog) {
        if(nullCheck(getDialog(), dialog)) {
            this.dialog = dialog;
            return true;
        }
        return false;
    }

    public Transition getTransition() {
        return transition;
    }
    public boolean setTransition(Transition transition) {
        if(nullCheck(getTransition(), transition)) {
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

    public ArrayList<Integer> getGotoSceneArrayList() {
        return gotoSceneArrayList;
    }
    public void setGotoSceneArrayList(ArrayList<Integer> gotoSceneArrayList) {
        this.gotoSceneArrayList = gotoSceneArrayList;
    }
}
