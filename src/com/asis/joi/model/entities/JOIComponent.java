package com.asis.joi.model.entities;

import javafx.beans.property.ReadOnlyStringWrapper;
import org.json.JSONObject;
import org.json.JSONString;

public abstract class JOIComponent implements JSONString, Cloneable, ComponentVisitable {
    private int componentId; //AKA sceneId | same thing
    private double layoutXPosition=0, layoutYPosition=10;
    private final ReadOnlyStringWrapper componentTitle = new ReadOnlyStringWrapper("Undefined");

    protected JOIComponent(int componentId) {
        setComponentId(componentId);
    }

    public String toJSONString() {
        return toJSON().toString(4);
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        object.put("sceneId", getComponentId());
        object.put("sceneTitle", getComponentTitle());
        object.put("layoutXPosition", getLayoutXPosition());
        object.put("layoutYPosition", getLayoutYPosition());

        return object;
    }

    protected static void createEntity(JSONObject jsonObject, JOIComponent joiComponent) {
        for (String key : jsonObject.keySet()) {
            switch (key) {
                case "sceneId":
                    joiComponent.setComponentId(jsonObject.getInt(key));
                    break;
                case "sceneTitle":
                    joiComponent.setComponentTitle(jsonObject.getString(key));
                    break;
                case "layoutXPosition":
                    joiComponent.setLayoutXPosition(jsonObject.getDouble(key));
                    break;
                case "layoutYPosition":
                    joiComponent.setLayoutYPosition(jsonObject.getDouble(key));
                    break;
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        JOIComponent component = (JOIComponent) super.clone();

        component.setLayoutXPosition(getLayoutXPosition());
        component.setLayoutYPosition(getLayoutYPosition());
        component.setComponentId(getComponentId());
        component.setComponentTitle(getComponentTitle());

        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JOIComponent)) return false;

        JOIComponent component = (JOIComponent) o;

        if (getComponentId() != component.getComponentId()) return false;
        if (Double.compare(component.getLayoutXPosition(), getLayoutXPosition()) != 0) return false;
        if (Double.compare(component.getLayoutYPosition(), getLayoutYPosition()) != 0) return false;
        return getComponentTitle() != null ? getComponentTitle().equals(component.getComponentTitle()) : component.getComponentTitle() == null;
    }

    //Getters and Setters
    public int getComponentId() {
        return componentId;
    }
    public void setComponentId(int componentId) {
        this.componentId = componentId;
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

    public String getComponentTitle() {
        return componentTitle.get();
    }
    public void setComponentTitle(String componentTitle) {
        this.componentTitle.set(componentTitle);
    }
    public ReadOnlyStringWrapper componentTitleProperty() {
        return componentTitle;
    }
}
