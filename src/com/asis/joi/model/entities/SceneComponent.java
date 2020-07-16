package com.asis.joi.model.entities;

public interface SceneComponent<T> {
    String jsonKeyName();
    T toJSON();
    double getDuration();
    Object clone() throws CloneNotSupportedException;
    boolean equals(Object o);
}
