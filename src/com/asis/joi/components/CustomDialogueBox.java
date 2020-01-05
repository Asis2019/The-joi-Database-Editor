package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class CustomDialogueBox implements JOISystemInterface, FirstLevelEffect {
    private File image;
    private double yScale=1, xScale=1, yPositionOffset=0, xPositionOffset=0, yTextPositionOffset=0, xTextPositionOffset=0;

    public CustomDialogueBox(File image) {
        setImage(image);
    }

    public JSONObject getCustomDialogueBoxAsJson() {
        JSONObject object = new JSONObject();

        object.put("image", getImage().getName());
        object.put("yScale", getYScale());
        object.put("xScale", getXScale());
        object.put("yPositionOffset", getYPositionOffset());
        object.put("xPositionOffset", getXPositionOffset());
        object.put("yTextPositionOffset", getYTextPositionOffset());
        object.put("xTextPositionOffset", getXTextPositionOffset());

        return object;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            setValueAccordingToKey(jsonObject, keys.next(), importDirectory);
        }
    }

    private void setValueAccordingToKey(JSONObject object, String key, File importDirectory) {
        switch (key) {
            case "image":
                setImage(new File(importDirectory.getPath()+File.separator+object.getString("image")));
                break;
            case "yScale":
                setYScale(object.getDouble("yScale"));
                break;
            case "xScale":
                setXScale(object.getDouble("xScale"));
                break;
            case "yPositionOffset":
                setYPositionOffset(object.getDouble("yPositionOffset"));
                break;
            case "xPositionOffset":
                setXPositionOffset(object.getDouble("xPositionOffset"));
                break;
            case "yTextPositionOffset":
                setYTextPositionOffset(object.getDouble("yTextPositionOffset"));
                break;
            case "xTextPositionOffset":
                setXTextPositionOffset(object.getDouble("xTextPositionOffset"));
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomDialogueBox)) return false;

        CustomDialogueBox that = (CustomDialogueBox) o;

        if (Double.compare(that.yScale, yScale) != 0) return false;
        if (Double.compare(that.xScale, xScale) != 0) return false;
        if (Double.compare(that.yPositionOffset, yPositionOffset) != 0) return false;
        if (Double.compare(that.xPositionOffset, xPositionOffset) != 0) return false;
        if (Double.compare(that.yTextPositionOffset, yTextPositionOffset) != 0) return false;
        if (Double.compare(that.xTextPositionOffset, xTextPositionOffset) != 0) return false;
        return getImage().equals(that.getImage());
    }

    public File getImage() {
        return image;
    }
    public void setImage(File image) {
        this.image = image;
    }

    public double getYScale() {
        return yScale;
    }
    public void setYScale(double yScale) {
        this.yScale = yScale;
    }

    public double getXScale() {
        return xScale;
    }
    public void setXScale(double xScale) {
        this.xScale = xScale;
    }

    public double getYPositionOffset() {
        return yPositionOffset;
    }
    public void setYPositionOffset(double yPositionOffset) {
        this.yPositionOffset = yPositionOffset;
    }

    public double getXPositionOffset() {
        return xPositionOffset;
    }
    public void setXPositionOffset(double xPositionOffset) {
        this.xPositionOffset = xPositionOffset;
    }

    public double getYTextPositionOffset() {
        return yTextPositionOffset;
    }
    public void setYTextPositionOffset(double yTextPositionOffset) {
        this.yTextPositionOffset = yTextPositionOffset;
    }

    public double getXTextPositionOffset() {
        return xTextPositionOffset;
    }
    public void setXTextPositionOffset(double xTextPositionOffset) {
        this.xTextPositionOffset = xTextPositionOffset;
    }
}
