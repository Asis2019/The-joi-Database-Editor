package com.asis.joi.model.entities;

import com.asis.joi.JOIPackageManager;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;

public class CustomDialogueBox implements JSONString, SceneComponent<JSONObject> {
    private File image;
    private double yScale = 1, xScale = 1, yPositionOffset = 0, xPositionOffset = 0, yTextPositionOffset = 0, xTextPositionOffset = 0;

    public static CustomDialogueBox createEntity(JSONObject jsonObject) {
        CustomDialogueBox customDialogueBox = new CustomDialogueBox();

        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "image":
                    customDialogueBox.setImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getString("image")));
                    break;
                case "yScale":
                    customDialogueBox.setYScale(jsonObject.getDouble("yScale"));
                    break;
                case "xScale":
                    customDialogueBox.setXScale(jsonObject.getDouble("xScale"));
                    break;
                case "yPositionOffset":
                    customDialogueBox.setYPositionOffset(jsonObject.getDouble("yPositionOffset"));
                    break;
                case "xPositionOffset":
                    customDialogueBox.setXPositionOffset(jsonObject.getDouble("xPositionOffset"));
                    break;
                case "yTextPositionOffset":
                    customDialogueBox.setYTextPositionOffset(jsonObject.getDouble("yTextPositionOffset"));
                    break;
                case "xTextPositionOffset":
                    customDialogueBox.setXTextPositionOffset(jsonObject.getDouble("xTextPositionOffset"));
                    break;
            }
        }

        return customDialogueBox;
    }

    @Override
    public String jsonKeyName() {
        return "customDialogueBox";
    }

    @Override
    public JSONObject toJSON() {
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
    public double getDuration() {
        return 0;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public CustomDialogueBox clone() throws CloneNotSupportedException {
        CustomDialogueBox customDialogueBox = (CustomDialogueBox) super.clone();

        customDialogueBox.setImage(new File(getImage().toURI()));
        customDialogueBox.setXPositionOffset(getXPositionOffset());
        customDialogueBox.setYPositionOffset(getYPositionOffset());
        customDialogueBox.setXTextPositionOffset(getXTextPositionOffset());
        customDialogueBox.setYTextPositionOffset(getYTextPositionOffset());
        customDialogueBox.setXScale(getXScale());
        customDialogueBox.setYScale(getYScale());

        return customDialogueBox;
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
