package com.asis.joi.model.entites;

import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

public class Transition implements JSONString, JOIEntity<JSONArray>, Cloneable {
    private String fadeColor, transitionTextColor="#ffffff", transitionTextOutlineColor="#000000", transitionText;
    private int waitTime = 0;
    private double fadeSpeed = 1; //stored as seconds

    public static Transition createEntity(JSONObject jsonObject) {
        Transition transition = new Transition();

        for (String key: jsonObject.keySet()) {
            switch (key) {
                case "fadeColor":
                    transition.setFadeColor(jsonObject.getString("fadeColor"));
                    break;
                case "transitionTextColor":
                    transition.setTransitionTextColor(jsonObject.getString("transitionTextColor"));
                    break;
                case "transitionTextOutlineColor":
                    transition.setTransitionTextOutlineColor(jsonObject.getString("transitionTextOutlineColor"));
                    break;
                case "transitionText":
                    transition.setTransitionText(jsonObject.getString("transitionText"));
                    break;
                case "waitTime":
                    transition.setWaitTime(jsonObject.getInt("waitTime"));
                    break;
                case "fadeSpeed":
                    transition.setFadeSpeed(convertSecondsToGameTime(jsonObject.getDouble("fadeSpeed")));
                    break;
            }
        }

        return transition;
    }

    private static double convertSecondsToGameTime(double timeInSeconds) {
        final double fadeSpeed = 1 / (timeInSeconds * 60);
        return AsisUtils.clamp(fadeSpeed, 0.0000000001, 5);
    }

    @Override
    public JSONArray toJSON() {
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

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public Transition clone() throws CloneNotSupportedException {
        Transition transition = (Transition) super.clone();

        transition.setFadeColor(getFadeColor());
        transition.setFadeSpeed(getFadeSpeed());
        transition.setTransitionText(getTransitionText());
        transition.setTransitionTextColor(getTransitionTextColor());
        transition.setTransitionTextOutlineColor(getTransitionTextOutlineColor());
        transition.setWaitTime(getWaitTime());

        return transition;
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
