package com.asis.joi.model.entites;

import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIEntity;
import com.asis.joi.model.entites.dialog.Dialog;
import com.asis.utilities.AsisUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Scene implements JSONString, JOIEntity<JSONObject>, Cloneable {
    private int sceneId;
    private double layoutXPosition, layoutYPosition;
    private String sceneTitle;
    private File sceneImage;

    private final ReadOnlyBooleanWrapper badEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper goodEnd = new ReadOnlyBooleanWrapper();

    private final ArrayList<SceneComponent<?>> sceneComponents = new ArrayList<>();

    public Scene() {
        this(0, 0, 10, "Scene 1");
    }

    public Scene(int sceneId) {
        this(sceneId, 0, 10, "Scene " + (sceneId + 1));
    }

    public Scene(int sceneId, int layoutXPosition, int layoutYPosition, String sceneTitle) {
        setSceneId(sceneId);
        setLayoutXPosition(layoutXPosition);
        setLayoutYPosition(layoutYPosition);
        setSceneTitle(sceneTitle);
        addComponent(new Transition());
        addComponent(new LineGroup());
    }

    public void addComponent(SceneComponent<?> sceneComponent) {
        getSceneComponents().add(sceneComponent);
    }

    public <T extends SceneComponent<?>> void removeComponent(Class<T> componentClass) {
        getSceneComponents().remove(getComponent(componentClass));
    }

    /**
     * Checks if scene has the component passed in. Returns true if the passed component exists in the components list.
     *
     * @param component - the scene component to check for
     * @return boolean
     */
    public boolean hasComponent(Class<?> component) {
        for (SceneComponent<?> sceneComponent : getSceneComponents())
            if (sceneComponent.getClass() == component) return true;

        return false;
    }

    /**
     * Checks if the scene contains the exact component passed in
     *
     * @param component - the scene component to check for
     * @return boolean
     */
    public boolean containsComponent(SceneComponent<?> component) {
        for (SceneComponent<?> sceneComponent : getSceneComponents())
            if (sceneComponent == component) return true;

        return false;
    }

    public <T extends SceneComponent<?>> T getComponent(Class<T> componentName) throws NoSuchElementException {
        for (SceneComponent<?> sceneComponent : getSceneComponents())
            if (sceneComponent.getClass() == componentName) return (T) sceneComponent;

        throw new NoSuchElementException();
    }

    public static Scene createEntity(JSONObject jsonObject) {
        Scene scene = new Scene();

        //Note scene id is not set here, it is set by the joi class
        //Set other fields
        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "sceneId":
                    scene.setSceneId(jsonObject.getInt("sceneId"));
                    break;
                case "sceneTitle":
                    scene.setSceneTitle(jsonObject.getString("sceneTitle"));
                    break;
                case "layoutXPosition":
                    scene.setLayoutXPosition(jsonObject.getDouble("layoutXPosition"));
                    break;
                case "layoutYPosition":
                    scene.setLayoutYPosition(jsonObject.getDouble("layoutYPosition"));
                    break;
                case "joiEnd":
                    scene.setGoodEnd(true);
                    break;
                case "badJoiEnd":
                    scene.setBadEnd(true);
                    break;
                case "sceneImage":
                    if (jsonObject.get("sceneImage") instanceof JSONObject) {
                        scene.setSceneImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getJSONObject("sceneImage").getString("name")));
                    } else {
                        scene.setSceneImage(new File(JOIPackageManager.getInstance().getJoiPackageDirectory().getPath() + File.separator + jsonObject.getString("sceneImage")));
                    }
                    break;
                case "noFade":
                    scene.removeComponent(Transition.class);
                    break;
                case "transition":
                    scene.removeComponent(Transition.class);
                    scene.addComponent(Transition.createEntity(jsonObject.getJSONArray("transition").getJSONObject(0)));
                    break;
                case "timer":
                    scene.addComponent(Timer.createEntity(jsonObject.getJSONArray("timer").getJSONObject(0)));
                    break;
                case "customDialogueBox":
                    scene.addComponent(CustomDialogueBox.createEntity(jsonObject.getJSONObject("customDialogueBox")));
                    break;
                case "dialogChoice":
                    scene.addComponent(Dialog.createEntity(jsonObject.getJSONArray("dialogChoice").getJSONObject(0)));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt("gotoScene")}));

                    scene.addComponent(GotoScene.createEntity(gotoObject));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRObject = new JSONObject();
                    gotoRObject.put("array", jsonObject.getJSONArray("gotoSceneInRange"));

                    scene.addComponent(GotoScene.createEntity(gotoRObject));
                    break;
            }
        }

        //set lines
        int i = 0;
        while (jsonObject.has("line" + i)) {
            LineGroup lineGroup = scene.getComponent(LineGroup.class);
            lineGroup.getLineArrayList().add(Line.createEntity(jsonObject.getJSONArray("line" + i).getJSONObject(0), i));
            i++;
        }

        return scene;
    }

    public double getDuration() {
        double totalTime = 0;
        for(SceneComponent<?> sceneComponent: getSceneComponents()) totalTime+=sceneComponent.getDuration();
        return totalTime;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject sceneObject = new JSONObject();

        sceneObject.put("sceneId", getSceneId());
        sceneObject.put("sceneTitle", getSceneTitle());
        sceneObject.put("layoutXPosition", getLayoutXPosition());
        sceneObject.put("layoutYPosition", getLayoutYPosition());

        if (goodEndProperty().getValue()) sceneObject.put("joiEnd", true);
        if (badEndProperty().getValue()) sceneObject.put("badJoiEnd", true);
        if (getSceneImage() != null) sceneObject.put("sceneImage", getSceneImage().getName());

        //Add any and all components to the json object
        for(SceneComponent<?> component: getSceneComponents()) {
            if(component.jsonKeyName() != null)
                sceneObject.put(component.jsonKeyName(), component.toJSON());
        }

        //If no transition component exists, put noFade
        if(!hasComponent(Transition.class)) sceneObject.put("noFade", true);

        //Merge lineGroup object into sceneObject
        if(getComponent(LineGroup.class).getLineArrayList().size() > 0)
            sceneObject = AsisUtils.mergeObject(sceneObject, getComponent(LineGroup.class).toJSON());

        //Merge GotoScene object into sceneObject
        if(hasComponent(GotoScene.class))
            sceneObject = AsisUtils.mergeObject(sceneObject, getComponent(GotoScene.class).toJSON());

        return sceneObject;
    }

    @Override
    public Scene clone() throws CloneNotSupportedException {
        Scene scene = (Scene) super.clone();

        scene.setBadEnd(isBadEnd());
        scene.setGoodEnd(isGoodEnd());
        scene.setLayoutXPosition(getLayoutXPosition());
        scene.setLayoutYPosition(getLayoutYPosition());
        scene.setSceneId(getSceneId());
        scene.setSceneTitle(getSceneTitle());

        scene.setSceneImage(getSceneImage());

        for (SceneComponent<?> sceneComponent: getSceneComponents())
            scene.addComponent((SceneComponent<?>) sceneComponent.clone());

        return scene;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Scene)) return false;

        Scene scene = (Scene) object;

        if (getSceneId() != scene.getSceneId()) return false;
        if (Double.compare(scene.getLayoutXPosition(), getLayoutXPosition()) != 0) return false;
        if (Double.compare(scene.getLayoutYPosition(), getLayoutYPosition()) != 0) return false;
        if (getSceneTitle() != null ? !getSceneTitle().equals(scene.getSceneTitle()) : scene.getSceneTitle() != null)
            return false;
        if (getSceneImage() != null ? !getSceneImage().equals(scene.getSceneImage()) : scene.getSceneImage() != null)
            return false;
        if (!getSceneComponents().equals(scene.getSceneComponents())) return false;
        if (isBadEnd() != scene.isBadEnd()) return false;
        return isGoodEnd() == scene.isGoodEnd();
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

    public File getSceneImage() {
        return sceneImage;
    }

    public void setSceneImage(File sceneImage) {
        this.sceneImage = sceneImage;
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

    public ArrayList<SceneComponent<?>> getSceneComponents() {
        return sceneComponents;
    }
}
