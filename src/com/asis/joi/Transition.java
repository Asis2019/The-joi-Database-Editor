package com.asis.joi;

import org.json.JSONArray;
import org.json.JSONObject;

public class Transition {
    private String fadeColor, transitionTextColor, transitionTextOutlineColor, transitionText;
    private int waitTime;
    private double fadeSpeed;

    public Transition() {
        this("#ffffff", "#000000", 0, 0.02);
    }
    public Transition(String transitionTextColor, String transitionTextOutlineColor, int waitTime, double fadeSpeed) {
        setTransitionTextColor(transitionTextColor);
        setTransitionTextOutlineColor(transitionTextOutlineColor);
        setWaitTime(waitTime);
        setFadeSpeed(fadeSpeed);
    }

    public JSONArray getTransitionAsJson() {
        JSONObject data = new JSONObject();
        if(getFadeColor() != null) data.put("fadeColor", getFadeColor());
        if(getTransitionTextColor() != null) data.put("transitionTextColor", getTransitionTextColor());
        if(getTransitionTextOutlineColor() != null) data.put("transitionTextOutlineColor", getTransitionTextOutlineColor());
        if(getTransitionText() != null) data.put("transitionText", getTransitionText());
        data.put("waitTime", getWaitTime());
        data.put("fadeSpeed", getFadeSpeed());

        JSONArray transitionArray = new JSONArray();
        return transitionArray.put(data);
    }

    @Override
    public String toString() {
        return getTransitionAsJson().toString(4);
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
