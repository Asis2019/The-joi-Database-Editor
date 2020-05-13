package com.asis.joi.model.entites;

import com.asis.joi.JOIPackageManager;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;

public class SceneImage implements JSONString, SceneComponent<JSONObject> {
    private File image;
    private int frames = -1;
    private double frameRate = -1;

    public static SceneImage createEntity(String string) {
        JSONObject sceneImageObject = new JSONObject();
        sceneImageObject.put("name", string);
        return createEntity(sceneImageObject);
    }

    public static SceneImage createEntity(JSONObject jsonObject) {
        SceneImage sceneImage = new SceneImage();

        for (String key: jsonObject.keySet()) {
            switch (key) {
                case "name":
                    sceneImage.setImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getString("name")));
                    break;
                case "frames":
                    sceneImage.setFrames(jsonObject.getInt("frames"));
                    break;
                case "frameRate":
                    sceneImage.setFrameRate(jsonObject.getDouble("frameRate"));
                    break;
            }
        }

        return sceneImage;
    }

    @Override
    public String jsonKeyName() {
        return "sceneImage";
    }

    @Override
    public JSONObject toJSON() {
        if(getFrameRate() != -1 && getFrames() != -1) {
            JSONObject sceneImageObject = new JSONObject();

            sceneImageObject.put("name", getImage().getName());
            sceneImageObject.put("frames", getFrames());
            sceneImageObject.put("frameRate", getFrameRate());

            return sceneImageObject;
        }

        return null;
    }

    @Override
    public double getDuration() {
        return 0;
    }

    @Override
    public SceneImage clone() throws CloneNotSupportedException {
        SceneImage sceneImage = (SceneImage) super.clone();

        sceneImage.setFrameRate(getFrameRate());
        sceneImage.setFrames(getFrames());
        sceneImage.setImage(getImage());

        return sceneImage;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SceneImage)) return false;

        SceneImage that = (SceneImage) o;

        if (getFrames() != that.getFrames()) return false;
        if (Double.compare(that.getFrameRate(), getFrameRate()) != 0) return false;
        return getImage() != null ? getImage().equals(that.getImage()) : that.getImage() == null;
    }

    //Getters and Setters
    public File getImage() {
        return image;
    }
    public void setImage(File image) {
        this.image = image;
    }

    public int getFrames() {
        return frames;
    }
    public void setFrames(int frames) {
        this.frames = frames;
    }

    public double getFrameRate() {
        return frameRate;
    }
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }
}
