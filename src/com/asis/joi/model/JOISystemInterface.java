package com.asis.joi.model;

import org.json.JSONObject;

import java.io.File;

public interface JOISystemInterface {
    void setDataFromJson(JSONObject jsonObject, File importDirectory);
}
