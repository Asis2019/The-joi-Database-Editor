package com.asis.joi.model.components;

import com.asis.joi.model.JOISystemInterface;
import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class Transition implements JOISystemInterface, FirstLevelEffect {
    private String fadeColor, transitionTextColor="#ffffff", transitionTextOutlineColor="#000000", transitionText;
    private int waitTime = 0;
    private double fadeSpeed = 1; //stored as seconds

    public JSONArray getTransitionAsJson() {
        JSONObject data = new JSONObject();
        if(getFadeColor() != null) data.put("fadeColor", getFadeColor());
        if(getTransitionTextColor() != null) data.put("transitionTextColor", getTransitionTextColor());
        if(getTransitionTextOutlineColor() != null) data.put("transitionTextOutlineColor", getTransitionTextOutlineColor());
        if(getTransitionText() != null) data.put("transitionText", getTransitionText());
        if(getWaitTime() != 0) data.put("waitTime", getWaitTime()); //Do to a game bug waitTime can't be 0 or the game will not finish the transition
        data.put("fadeSpeed", convertSecondsToGameTime(getFadeSpeed()));

        JSONArray transitionArray = new JSONArray();
        return transitionArray.put(data);
    }

    private static double convertSecondsToGameTime(double timeInSeconds) {
        final double fadeSpeed = 1 / (timeInSeconds * 60);
        return AsisUtils.clamp(fadeSpeed, 0.0000000001, 5);
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        setData(jsonObject.keys(), jsonObject);
    }

    private void setData(Iterator<String> keys, JSONObject object) {
        while (keys.hasNext()) {
            setValueAccordingToKey(object, keys.next());
        }
    }

    private void setValueAccordingToKey(JSONObject jsonObject, String key) {
        switch (key) {
            case "fadeColor":
                setFadeColor(jsonObject.getString("fadeColor"));
                break;
            case "transitionTextColor":
                setTransitionTextColor(jsonObject.getString("transitionTextColor"));
                break;
            case "transitionTextOutlineColor":
                setTransitionTextOutlineColor(jsonObject.getString("transitionTextOutlineColor"));
                break;
            case "transitionText":
                setTransitionText(jsonObject.getString("transitionText"));
                break;
            case "waitTime":
                setWaitTime(jsonObject.getInt("waitTime"));
                break;
            case "fadeSpeed":
                setFadeSpeed(convertSecondsToGameTime(jsonObject.getDouble("fadeSpeed")));
                break;
        }
    }

    @Override
    public String toString() {
        return getTransitionAsJson().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Transition)) return false;

        Transition that = (Transition) object;

        if (getWaitTime() != that.getWaitTime()) return false;
        if (Double.compare(that.getFadeSpeed(), getFadeSpeed()) != 0) return false;
        if (getFadeColor() != null ? !getFadeColor().equals(that.getFadeColor()) : that.getFadeColor() != null)
            return false;
        if (!getTransitionTextColor().equals(that.getTransitionTextColor())) return false;
        if (!getTransitionTextOutlineColor().equals(that.getTransitionTextOutlineColor())) return false;
        return getTransitionText() != null ? getTransitionText().equals(that.getTransitionText()) : that.getTransitionText() == null;
    }

    //Getters and Setters
    public double getFadeSpeed() {
        return fadeSpeed;
    }
    public void setFadeSpeed(double fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }

    public int getWaitTime() {
        return waitTime;
    }
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public String getFadeColor() {
        return fadeColor;
    }
    public void setFadeColor(String fadeColor) {
        this.fadeColor = fadeColor;
    }

    public String getTransitionTextColor() {
        return transitionTextColor;
    }
    public void setTransitionTextColor(String transitionTextColor) {
        this.transitionTextColor = transitionTextColor;
    }

    public String getTransitionTextOutlineColor() {
        return transitionTextOutlineColor;
    }
    public void setTransitionTextOutlineColor(String transitionTextOutlineColor) {
        this.transitionTextOutlineColor = transitionTextOutlineColor;
    }

    public String getTransitionText() {
        return transitionText;
    }
    public void setTransitionText(String transitionText) {
        this.transitionText = transitionText;
    }
}
