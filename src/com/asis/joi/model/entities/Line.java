package com.asis.joi.model.entities;


import com.asis.joi.JOIPackageManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;

public class Line implements JSONString, SceneComponent<JSONArray> {
    private String fillColor = "#ffffff", outlineColor = "#000000", text = "";
    private File lineImage;
    private int lineNumber;

    //Beats
    private Boolean startBeat, stopBeat;
    private Double changeBeatPitch, frameRateMultiplier;
    private Integer changeBeatSpeed;

    private Boolean stopAmbience;

    public static Line createEntity(JSONObject jsonObject, int lineNumber) {
        jsonObject.put("id", lineNumber);
        return createEntity(jsonObject);
    }

    public static Line createEntity(JSONObject jsonObject) {
        Line line = new Line();

        for (String key : jsonObject.keySet()) {
            switch (key) {
                //id is the same as line number or X in "lineX". Only used in the editor as a pass through, does not get exported
                case "id":
                    line.setLineNumber(jsonObject.getInt("id"));
                    break;
                case "fillColor":
                    line.setFillColor(jsonObject.getString("fillColor"));
                    break;
                case "outlineColor":
                    line.setOutlineColor(jsonObject.getString("outlineColor"));
                    break;
                case "text":
                    line.setText(jsonObject.getString("text"));
                    break;
                case "lineImage":
                    line.setLineImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getString("lineImage")));
                    break;
                case "startBeat":
                    line.setStartBeat(jsonObject.getBoolean("startBeat"));
                    break;
                case "stopBeat":
                    line.setStopBeat(jsonObject.getBoolean("stopBeat"));
                    break;
                case "stop_ambience":
                    line.setStopAmbience(jsonObject.getBoolean("stop_ambience"));
                    break;
                case "changeBeatPitch":
                    line.setChangeBeatPitch(jsonObject.getDouble("changeBeatPitch"));
                    break;
                case "changeBeatSpeed":
                    line.setChangeBeatSpeed(jsonObject.getInt("changeBeatSpeed"));
                    break;
                case "frameRateMultiplier":
                    line.setFrameRateMultiplier(jsonObject.getDouble("frameRateMultiplier"));
                    break;
            }
        }

        return line;
    }

    public double getDuration() {
        //15d is the characters read in one second
        //2 is an average delay before next line shows
        return getText().length() / 15d + 2;
    }

    @Override
    public String jsonKeyName() {
        return "line" + getLineNumber();
    }

    @Override
    public JSONArray toJSON() {
        JSONObject object = new JSONObject();
        object.put("fillColor", getFillColor());
        object.put("outlineColor", getOutlineColor());
        object.put("text", getText());

        if (getLineImage() != null) object.put("lineImage", getLineImage().getName());
        if (getStartBeat() != null) object.put("startBeat", getStartBeat());
        if (getStopBeat() != null) object.put("stopBeat", getStopBeat());
        if (getStopAmbience() != null) object.put("stop_ambience", getStopAmbience());
        if (getChangeBeatPitch() != null) object.put("changeBeatPitch", getChangeBeatPitch());
        if (getChangeBeatSpeed() != null) object.put("changeBeatSpeed", getChangeBeatSpeed());
        if (getFrameRateMultiplier() != null) object.put("frameRateMultiplier", getFrameRateMultiplier());

        return new JSONArray().put(object);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public Line clone() throws CloneNotSupportedException {
        Line line = (Line) super.clone();

        line.setChangeBeatPitch(getChangeBeatPitch());
        line.setChangeBeatSpeed(getChangeBeatSpeed());
        line.setStartBeat(getStartBeat());
        line.setStopBeat(getStopBeat());
        line.setFillColor(getFillColor());
        line.setOutlineColor(getOutlineColor());
        line.setText(getText());
        line.setLineImage(getLineImage());
        line.setLineNumber(getLineNumber());
        line.setFrameRateMultiplier(getFrameRateMultiplier());
        line.setStopAmbience(getStopAmbience());

        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line)) return false;

        Line line = (Line) o;

        if (getLineNumber() != line.getLineNumber()) return false;
        if (!getFillColor().equals(line.getFillColor())) return false;
        if (!getOutlineColor().equals(line.getOutlineColor())) return false;
        if (!getText().equals(line.getText())) return false;
        if (getLineImage() != null ? !getLineImage().equals(line.getLineImage()) : line.getLineImage() != null)
            return false;
        if (getStartBeat() != null ? !getStartBeat().equals(line.getStartBeat()) : line.getStartBeat() != null)
            return false;
        if (getStopBeat() != null ? !getStopBeat().equals(line.getStopBeat()) : line.getStopBeat() != null)
            return false;
        if (getStopAmbience() != null ? !getStopAmbience().equals(line.getStopAmbience()) : line.getStopAmbience() != null)
            return false;
        if (getChangeBeatPitch() != null ? !getChangeBeatPitch().equals(line.getChangeBeatPitch()) : line.getChangeBeatPitch() != null)
            return false;
        if (getFrameRateMultiplier() != null ? !getFrameRateMultiplier().equals(line.getFrameRateMultiplier()) : line.getFrameRateMultiplier() != null)
            return false;
        return getChangeBeatSpeed() != null ? getChangeBeatSpeed().equals(line.getChangeBeatSpeed()) : line.getChangeBeatSpeed() == null;
    }

    //Getters and Setters
    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(String outlineColor) {
        this.outlineColor = outlineColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getLineImage() {
        return lineImage;
    }

    public void setLineImage(File lineImage) {
        this.lineImage = lineImage;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Boolean getStartBeat() {
        return startBeat;
    }

    public void setStartBeat(Boolean startBeat) {
        this.startBeat = startBeat;
    }

    public Boolean getStopBeat() {
        return stopBeat;
    }

    public void setStopBeat(Boolean stopBeat) {
        this.stopBeat = stopBeat;
    }

    public Double getChangeBeatPitch() {
        return changeBeatPitch;
    }

    public void setChangeBeatPitch(Double changeBeatPitch) {
        this.changeBeatPitch = changeBeatPitch;
    }

    public Integer getChangeBeatSpeed() {
        return changeBeatSpeed;
    }

    public void setChangeBeatSpeed(Integer changeBeatSpeed) {
        this.changeBeatSpeed = changeBeatSpeed;
    }

    public Double getFrameRateMultiplier() {
        return frameRateMultiplier;
    }

    public void setFrameRateMultiplier(Double frameRateMultiplier) {
        this.frameRateMultiplier = frameRateMultiplier;
    }

    public Boolean getStopAmbience() {
        return stopAmbience;
    }
    public void setStopAmbience(Boolean stopAmbience) {
        this.stopAmbience = stopAmbience;
    }
}