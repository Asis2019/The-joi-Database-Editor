package com.asis.joi.model;

import com.asis.joi.model.entities.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class JOI implements JSONString, Cloneable {
    private int sceneIdCounter = 0;
    private ArrayList<JOIComponent> joiComponents = new ArrayList<>();

    public <T extends JOIComponent> void addNewComponent(Class<T> joiComponentClass, Integer... optional) {
        //Check if an id was passed
        int componentId = optional.length > 0 ? optional[0] : getSceneIdCounter();

        //Change the counter to the highest id
        if(componentId > getSceneIdCounter()) setSceneIdCounter(componentId);

        //Add new component
        try {
            getJoiComponents().add(joiComponentClass.getConstructor(int.class).newInstance(componentId));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        //Increment counter
        setSceneIdCounter(getSceneIdCounter()+1);
    }

    public boolean removeComponent(int sceneId) {
        JOIComponent component = getComponent(sceneId);
        if(component instanceof Group) {
            getJoiComponents().remove(((Group) component).getInputNodeData());
            getJoiComponents().remove(((Group) component).getOutputNodeData());
        }
        return getJoiComponents().remove(component);
    }

    public JOIComponent getComponent(int componentId) {
        for (JOIComponent component : getJoiComponents()) {
            if (component.getComponentId() == componentId) return component;
        }

        return null;
    }

    @Deprecated
    public Scene getScene(int sceneId) {
        JOIComponent component = getComponent(sceneId);
        if(component instanceof Scene) return (Scene) component;
        return null;
    }

    public static JOI createEntity(JSONObject jsonObject) {
        JOI joi = new JOI();

        JSONArray array = jsonObject.getJSONArray("JOI");
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).has("sceneId")) {
                if(array.getJSONObject(i).has("componentType") && array.getJSONObject(i).getString("componentType").equals("VariableSetter")) {
                    VariableSetter setter = VariableSetter.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(setter);
                } else if(array.getJSONObject(i).has("componentType") && array.getJSONObject(i).getString("componentType").equals("Conditional")) {
                    Condition condition = Condition.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(condition);
                } else if(array.getJSONObject(i).has("componentType") && array.getJSONObject(i).getString("componentType").equals("Arithmetic")) {
                    Arithmetic arithmetic = Arithmetic.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(arithmetic);
                } else if(array.getJSONObject(i).has("componentType") && array.getJSONObject(i).getString("componentType").equals("NodeGroup")) {
                    Group group = Group.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(group);
                } else if(array.getJSONObject(i).has("componentType") && array.getJSONObject(i).getString("componentType").equals("NodeGroupBridge")) {
                    GroupBridge groupBridge = GroupBridge.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(groupBridge);
                } else {
                    Scene scene = Scene.createEntity(array.getJSONObject(i));
                    joi.getJoiComponents().add(scene);
                }

                if (array.getJSONObject(i).getInt("sceneId") >= joi.getSceneIdCounter())
                    joi.setSceneIdCounter(array.getJSONObject(i).getInt("sceneId") + 1);
            } else {
                throw new RuntimeException("Scene id was not present for one or more of the scenes.");
            }
        }

        return joi;
    }

    public double getDuration() {
        //return getSceneArrayList().stream().mapToDouble(Scene::getDuration).sum();
        double totalTime = 0;
        for(JOIComponent joiComponent: getJoiComponents()) {
            if(joiComponent instanceof Scene)
                totalTime += ((Scene) joiComponent).getDuration();
        }
        return totalTime;
    }

    public JSONObject toJSON() {
        JSONArray joiArray = new JSONArray();

        for(JOIComponent component: getJoiComponents()) {
            if(component.toJSON() != null)
                joiArray.put(component.toJSON());
        }

        JSONObject finalWrapper = new JSONObject();
        return finalWrapper.put("JOI", joiArray);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public JOI clone() throws CloneNotSupportedException {
        JOI joi = (JOI) super.clone();

        ArrayList<JOIComponent> clonedArray = new ArrayList<>();
        for (JOIComponent component: getJoiComponents()) clonedArray.add((JOIComponent) component.clone());
        joi.setJoiComponents(clonedArray);

        joi.setSceneIdCounter(getSceneIdCounter());

        return joi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JOI)) return false;

        JOI joi = (JOI) o;

        if (getSceneIdCounter() != joi.getSceneIdCounter()) return false;
        return getJoiComponents().equals(joi.getJoiComponents());
    }

    public int getSceneIdCounter() {
        return sceneIdCounter;
    }
    private void setSceneIdCounter(int sceneIdCounter) {
        this.sceneIdCounter = sceneIdCounter;
    }
    public ArrayList<JOIComponent> getJoiComponents() {
        return joiComponents;
    }
    private void setJoiComponents(ArrayList<JOIComponent> joiComponents) {
        this.joiComponents = joiComponents;
    }

}
