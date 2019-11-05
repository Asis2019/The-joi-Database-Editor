package com.asis.joi.components;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class Line {
    private String fillColor, outlineColor, text;
    private File lineImage;
    private int lineNumber; //This is only applicable for the timers lines

    public Line() {
        this("#000000", "#ffffff");
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

        return new JSONArray().put(object);
    }

    @Override
    public String toString() {
        return getLineAsJson().toString(4);
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
}
