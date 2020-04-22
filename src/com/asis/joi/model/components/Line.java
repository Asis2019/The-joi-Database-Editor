package com.asis.joi.model.components;

import com.asis.joi.model.JOISystemInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class Line implements JOISystemInterface {
    private String fillColor, outlineColor, text="";
    private File lineImage;
    private int lineNumber;

    //Beats
    private Boolean startBeat, stopBeat;
    private Double changeBeatPitch;
    private Integer changeBeatSpeed;

    public Line() {
        this("#ffffff", "#000000");
    }
    public Line(int lineNumber) {
        this();
        setLineNumber(lineNumber);
    }
    public Line(String fillColor, String outlineColor) {
        setFillColor(fillColor);
        setOutlineColor(outlineColor);
    }

    public JSONArray getLineAsJson() {
        JSONObject object = new JSONObject();
        object.put("fillColor", getFillColor());
        object.put("outlineColor", getOutlineColor());
        object.put("text", getText());

        if(getLineImage() != null) object.put("lineImage", getLineImage().getName());
        if(getStartBeat() != null) object.put("startBeat", getStartBeat());
        if(getStopBeat() != null) object.put("stopBeat", getStopBeat());
        if(getChangeBeatPitch() != null) object.put("changeBeatPitch", getChangeBeatPitch());
        if(getChangeBeatSpeed() != null) object.put("changeBeatSpeed", getChangeBeatSpeed());

        return new JSONArray().put(object);
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            setValueAccordingToKey(jsonObject, keys.next(), importDirectory);
        }
    }

    private void setValueAccordingToKey(JSONObject jsonObject, String key, File importDirectory) {
        switch (key) {
            case "fillColor":
                setFillColor(jsonObject.getString("fillColor"));
                break;
            case "outlineColor":
                setOutlineColor(jsonObject.getString("outlineColor"));
                break;
            case "text":
                setText(jsonObject.getString("text"));
                break;
            case "lineImage":
                setLineImage(new File(importDirectory.getPath()+"/"+jsonObject.getString("lineImage")));
                break;
            case "startBeat":
                setStartBeat(jsonObject.getBoolean("startBeat"));
                break;
            case "stopBeat":
                setStopBeat(jsonObject.getBoolean("stopBeat"));
                break;
            case "changeBeatPitch":
                setChangeBeatPitch(jsonObject.getDouble("changeBeatPitch"));
                break;
            case "changeBeatSpeed":
                setChangeBeatSpeed(jsonObject.getInt("changeBeatSpeed"));
                break;
        }
    }

    @Override
    public String toString() {
        return getLineAsJson().toString(4);
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
        if (getChangeBeatPitch() != null ? !getChangeBeatPitch().equals(line.getChangeBeatPitch()) : line.getChangeBeatPitch() != null)
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
}
