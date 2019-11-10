package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import com.asis.joi.components.dialog.Dialog;
import com.asis.utilities.AsisUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Scene implements JOISystemInterface {
    private int sceneId;
    private double layoutXPosition, layoutYPosition;
    private String sceneTitle;
    private boolean noFade=false;
    private File sceneImage;
    private Timer timer;
    private Dialog dialog;
    private Transition transition = new Transition();
    private GotoScene gotoScene;
    private ArrayList<Line> lineArrayList = new ArrayList<>();

    private ReadOnlyBooleanWrapper badEnd = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper goodEnd = new ReadOnlyBooleanWrapper();

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
    }

    public void addNewLine(final int lineNumber) {
        getLineArrayList().add(new Line(lineNumber));
    }
    public void removeLine(final int lineNumber) {
        for(Line line: getLineArrayList()) {
            if(line.getLineNumber() > lineNumber) {
                line.setLineNumber(line.getLineNumber()-1);
            }
        }

        getLineArrayList().remove(getLine(lineNumber));
    }
    public Line getLine(int lineNumber) {
        for(Line line: getLineArrayList()) {
            if (line.getLineNumber() == lineNumber) {
                return line;
            }
        }
        return null;
    }

    public JSONObject getSceneAsJson() {
        //Make new Object
        JSONObject sceneObject = new JSONObject();

        //Set values
        setValues(sceneObject);

        return sceneObject;
    }

    private void setValues(JSONObject sceneObject) {
        sceneObject.put("sceneId", getSceneId());
        sceneObject.put("sceneTitle", getSceneTitle());
        sceneObject.put("layoutXPosition", getLayoutXPosition());
        sceneObject.put("layoutYPosition", getLayoutYPosition());

        //if(!getGotoSceneArrayList().isEmpty()) sceneObject.put("GotoScene", getGotoSceneArrayList().toArray());
        if(isNoFade()) sceneObject.put("noFade", true);
        if(goodEndProperty().getValue()) sceneObject.put("joiEnd", true);
        if(badEndProperty().getValue()) sceneObject.put("badJoiEnd", true);
        if(getSceneImage() != null) sceneObject.put("sceneImage", getSceneImage().getName());
        if(getTimer() != null) sceneObject.put("timer", getTimer().getTimerAsJson());
        if(getDialog() != null) sceneObject.put("dialogChoice", getDialog().getDialogAsJson());
        if(getTransition() != null) sceneObject.put("transition", getTransition().getTransitionAsJson());

        //Merge return object from gotoScene with current
        if(getGotoScene() != null) {
            AsisUtils.mergeJSONObjects(sceneObject, getGotoScene().getGotoSceneAsJson());
        }

        for(int i=0; i<getLineArrayList().size(); i++) {
            sceneObject.put("line"+i, getLineArrayList().get(i).getLineAsJson());
        }
    }

    @Override
    public void setDataFromJson(JSONObject object, File importDirectory) {

        //Set scene id
        if (object.has("sceneId")) {
            setSceneId(object.getInt("sceneId"));
        } else {
            //Throw scene id error
            throw new RuntimeException("Scene id was not present for one or more of the scenes.");
        }

        //Set other fields
        setData(object.keys(), object, importDirectory);

        //set lines
        int i=0;
        while(object.has("line"+i)) {
            addNewLine(i);
            getLineArrayList().get(i).setDataFromJson(object.getJSONArray("line" + i).getJSONObject(0), importDirectory);
            i++;
        }
    }

    private void setData(Iterator<String> keys, JSONObject object, File importDirectory) {
        while (keys.hasNext()) {
            setValueAccordingToKey(object, keys.next(), importDirectory);
        }
    }

    private void setValueAccordingToKey(JSONObject object, String key, File importDirectory) {
        switch (key) {
            case "sceneTitle":
                setSceneTitle(object.getString("sceneTitle"));
                break;
            case "layoutXPosition":
                setLayoutXPosition(object.getDouble("layoutXPosition"));
                break;
            case "layoutYPosition":
                setLayoutYPosition(object.getDouble("layoutYPosition"));
                break;
            case "joiEnd":
                setGoodEnd(true);
                break;
            case "badJoiEnd":
                setBadEnd(true);
                break;
            case "sceneImage":
                setSceneImage(new File(importDirectory.getPath()+"\\"+object.getString("sceneImage")));
                break;
            case "noFade":
                setNoFade(object.getBoolean("noFade"));
                break;
            case "transition":
                setTransition(new Transition());
                getTransition().setDataFromJson(object.getJSONArray("transition").getJSONObject(0), importDirectory);
                break;
            case "timer":
                setTimer(new Timer());
                getTimer().setDataFromJson(object.getJSONArray("timer").getJSONObject(0), importDirectory);
                break;
            case "dialogChoice":
                setDialog(new Dialog());
                getDialog().setDataFromJson(object.getJSONArray("dialogChoice").getJSONObject(0), importDirectory);
                break;
            case "gotoScene":
                setGotoScene(new GotoScene());
                getGotoScene().addValue(object.getInt("gotoScene"));
                break;
            case "gotoSceneInRange":
                setGotoScene(new GotoScene());
                getGotoScene().setDataFromJson(object.getJSONArray("gotoSceneInRange"));
                break;
        }
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
        if (transition == null) setNoFade(true);
        else setNoFade(false);

        this.transition = transition;
    }

    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }
    public void setLineArrayList(ArrayList<Line> lineArrayList) {
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
}
