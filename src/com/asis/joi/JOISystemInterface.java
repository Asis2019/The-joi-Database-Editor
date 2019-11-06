package com.asis.joi;

import org.json.JSONObject;

import java.io.File;

public interface JOISystemInterface {
    void setDataFromJson(JSONObject jsonObject, File importDirectory);
    String toString();
}
