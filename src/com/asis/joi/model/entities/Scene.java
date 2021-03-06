package com.asis.joi.model.entities;

import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.ui.asis_node.node_functional_expansion.ComponentVisitor;
import com.asis.utilities.AsisUtils;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Scene extends JOIComponent {
    private final ReadOnlyBooleanWrapper badEnd = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper goodEnd = new ReadOnlyBooleanWrapper();
    private String ambience;

    private ArrayList<SceneComponent<?>> sceneComponents = new ArrayList<>();

    public Scene() {
        this(0, 0, 10, "Scene 1");
    }

    public Scene(int sceneId) {
        this(sceneId, 0, 10, "Scene " + (sceneId + 1));
    }

    public Scene(int sceneId, int layoutXPosition, int layoutYPosition, String sceneTitle) {
        super(sceneId);
        setLayoutXPosition(layoutXPosition);
        setLayoutYPosition(layoutYPosition);
        setComponentTitle(sceneTitle);
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

    @SuppressWarnings({"unchecked"})
    public <T extends SceneComponent<?>> T getComponent(Class<T> componentName) throws NoSuchElementException {
        for (SceneComponent<?> sceneComponent : getSceneComponents())
            if (sceneComponent.getClass() == componentName) return (T) sceneComponent;

        throw new NoSuchElementException();
    }

    public static Scene createEntity(JSONObject jsonObject) {
        Scene scene = new Scene();
        createEntity(jsonObject, scene);

        //Note scene id is not set here, it is set by the joi class
        //Set other fields
        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "joiEnd":
                    scene.setGoodEnd(true);
                    break;
                case "badJoiEnd":
                    scene.setBadEnd(true);
                    break;
                case "sceneImage":
                    if (jsonObject.get("sceneImage") instanceof JSONObject)
                        scene.addComponent(SceneImage.createEntity(jsonObject.getJSONObject(key)));
                    else
                        scene.addComponent(SceneImage.createEntity(jsonObject.getString(key)));
                    break;
                case "noFade":
                    scene.removeComponent(Transition.class);
                    break;
                case "transition":
                    scene.removeComponent(Transition.class);
                    scene.addComponent(Transition.createEntity(jsonObject.getJSONArray(key).getJSONObject(0)));
                    break;
                case "timer":
                    scene.addComponent(Timer.createEntity(jsonObject.getJSONArray(key).getJSONObject(0)));
                    break;
                case "customDialogueBox":
                    scene.addComponent(CustomDialogueBox.createEntity(jsonObject.getJSONObject(key)));
                    break;
                case "dialogChoice":
                    scene.addComponent(Dialog.createEntity(jsonObject.getJSONArray(key).getJSONObject(0)));
                    break;
                case "gotoScene":
                    JSONObject gotoObject = new JSONObject();
                    gotoObject.put("array", new JSONArray(new int[]{jsonObject.getInt(key)}));

                    scene.addComponent(GotoScene.createEntity(gotoObject));
                    break;
                case "gotoSceneInRange":
                    JSONObject gotoRObject = new JSONObject();
                    gotoRObject.put("array", jsonObject.getJSONArray(key));

                    scene.addComponent(GotoScene.createEntity(gotoRObject));
                    break;
                case "ambience":
                    scene.setAmbience(jsonObject.getString(key));
            }
        }

        //set lines
        scene.removeComponent(LineGroup.class);
        if(jsonObject.has("line0")) scene.addComponent(new LineGroup());
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
        for (SceneComponent<?> sceneComponent : getSceneComponents()) totalTime += sceneComponent.getDuration();
        return totalTime;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject sceneObject = super.toJSON();
        if (getAmbience() != null) sceneObject.put("ambience", getAmbience());
        if (goodEndProperty().getValue()) sceneObject.put("joiEnd", true);
        if (badEndProperty().getValue()) sceneObject.put("badJoiEnd", true);

        //Add any and all components to the json object
        for (SceneComponent<?> component : getSceneComponents()) {
            if (component.jsonKeyName() != null && component.toJSON() != null)
                sceneObject.put(component.jsonKeyName(), component.toJSON());
        }

        //Put base SceneImage name into sceneObject if toJSON is null
        if (hasComponent(SceneImage.class) && getComponent(SceneImage.class).toJSON() == null)
            sceneObject.put(getComponent(SceneImage.class).jsonKeyName(), getComponent(SceneImage.class).getImage().getName());

        //If no transition component exists, put noFade
        if (!hasComponent(Transition.class)) sceneObject.put("noFade", true);

        //Merge lineGroup object into sceneObject
        try {
            if (getComponent(LineGroup.class).getLineArrayList().size() > 0)
                sceneObject = AsisUtils.mergeObject(sceneObject, getComponent(LineGroup.class).toJSON());
        } catch (NoSuchElementException ignore) {}

        //Merge GotoScene object into sceneObject
        if (hasComponent(GotoScene.class))
            sceneObject = AsisUtils.mergeObject(sceneObject, getComponent(GotoScene.class).toJSON());

        return sceneObject;
    }

    @Override
    public Scene clone() throws CloneNotSupportedException {
        Scene scene = (Scene) super.clone();

        scene.setBadEnd(isBadEnd());
        scene.setGoodEnd(isGoodEnd());
        scene.setAmbience(getAmbience());

        ArrayList<SceneComponent<?>> clonedArray = new ArrayList<>();
        for (SceneComponent<?> sceneComponent : getSceneComponents()) clonedArray.add((SceneComponent<?>) sceneComponent.clone());
        scene.setSceneComponents(clonedArray);

        return scene;
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scene)) return false;
        if (!super.equals(o)) return false;

        Scene scene = (Scene) o;

        isBadEnd();
        if (isBadEnd() != scene.isBadEnd()) return false;
        isGoodEnd();
        if (isGoodEnd() != scene.isGoodEnd()) return false;
        if (getAmbience() != null ? !getAmbience().equals(scene.getAmbience()) : scene.getAmbience() != null)
            return false;
        return getSceneComponents().equals(scene.getSceneComponents());
    }

    @Override
    public void accept(ComponentVisitor componentVisitor) {
        componentVisitor.visit(this);
    }

    //Getters and Setters
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

    private void setSceneComponents(ArrayList<SceneComponent<?>> sceneComponents) {
        this.sceneComponents = sceneComponents;
    }

    public String getAmbience() {
        return ambience;
    }

    public void setAmbience(String ambience) {
        this.ambience = ambience;
    }
}
