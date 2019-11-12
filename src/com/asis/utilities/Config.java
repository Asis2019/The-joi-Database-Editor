package com.asis.utilities;

import org.json.JSONObject;

import java.io.File;

public final class Config {

    private static File configurationFile = new File("settings.json");

    public static Object get(String key) {
        JSONObject object = AsisUtils.readJsonFromFile(getConfigurationFile());
        if(object != null && object.has(key)) {
            return object.get(key);
        }
        return key+" does not exists or settings file is invalid";
    }

    //Getters and setters
    public static File getConfigurationFile() {
        return configurationFile;
    }
    public void setConfigurationFile(File configurationFile) {
        Config.configurationFile = configurationFile;
    }
}
