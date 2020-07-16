package com.asis.joi.model.entities;

import javafx.beans.property.ReadOnlyStringWrapper;
import org.json.JSONObject;
import org.json.JSONString;

public abstract class JOIComponent implements JSONString, Cloneable {
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
    public abstract boolean equals(Object o);

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
