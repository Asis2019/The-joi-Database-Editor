package com.asis.joi.model.entites;

public interface SceneComponent<T> {
    String jsonKeyName();
    T toJSON();
    double getDuration();
    Object clone() throws CloneNotSupportedException;
}
