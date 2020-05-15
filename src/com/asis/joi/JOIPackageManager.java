package com.asis.joi;

import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.model.JOI;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.MetaData;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.asis.utilities.AsisUtils.deleteFolder;

public class JOIPackageManager {
    private File joiPackageDirectory = new File("defaultWorkspace");
    private final Set<String> joiPackageLanguages = new HashSet<>();
    private final ArrayList<JOIPackage> joiPackages = new ArrayList<>();
    private static final JOIPackageManager joiPackageManager = new JOIPackageManager();

    private JOIPackageManager() {
        if (!getJoiPackageDirectory().exists()) System.out.println(getJoiPackageDirectory().mkdir());
    }

    public static JOIPackageManager getInstance() {
        return joiPackageManager;
    }

    public void clear() {
        getJoiPackageLanguages().clear();
        getJoiPackages().clear();
    }

    public boolean changesHaveOccurred() {
        try {
            for(JOIPackage currentJoiPackage: getJoiPackages()) {
                final JOIPackage originalPackage = loadJoiPackage(currentJoiPackage.getPackageLanguageCode());
                if(!currentJoiPackage.equals(originalPackage)) return true;
            }
            return false;
        } catch (RuntimeException | IOException e) {
            return true;
        }
    }

    private void loadJoiPackageLanguages() {
        getJoiPackageLanguages().clear();

        Object data = Config.get("LANGUAGES");
        if (data instanceof JSONArray) {
            JSONArray jsonArray = ((JSONArray) data);
            for (int i = 0; i < jsonArray.length(); i++) {
                final String languageCode = jsonArray.getJSONObject(i).getString("file_code");

                File file = new File(getJoiPackageDirectory(), String.format("joi_text_%s.json", languageCode));
                if (file.exists()) getJoiPackageLanguages().add(languageCode);
            }
        }
    }

    private void loadJoiPackages() throws IOException {
        loadJoiPackageLanguages();

        getJoiPackages().clear();
        for (String languageCode : getJoiPackageLanguages()) addJOIPackage(loadJoiPackage(languageCode));
    }

    private JOIPackage loadJoiPackage(String languageCode) throws IOException {
        JOIPackage joiPackage = new JOIPackage();
        joiPackage.setPackageLanguageCode(languageCode);
        joiPackage.setJoi(importJoiFromDirectory(languageCode));
        joiPackage.setMetaData(importMetaDataFromDirectory(languageCode));
        return joiPackage;
    }

    public void exportJOIPackageAsFiles(File destination) {
        for(JOIPackage joiPackage: getJoiPackages()) {
            joiPackage.getMetaData().setEstimatedDuration(joiPackage.getJoi().getDuration());
            joiPackage.exportPackageAsFiles(destination);
        }
    }

    public void exportJOIPackageAsZip(File zipFile) {
        try {
            //Create temporary folder
            File temporaryDirectory = new File(zipFile.getParent() + "/tmp");

            //Delete contents of/and folder if present
            if (temporaryDirectory.isDirectory()) AsisUtils.deleteFolder(temporaryDirectory);

            //Make folder and begin process
            if (temporaryDirectory.mkdir()) {
                //Do normal export to temporary directory
                exportJOIPackageAsFiles(temporaryDirectory);

                //Compress temporary folder to zip file
                AsisUtils.writeDirectoryToZip(temporaryDirectory, zipFile);

                //Delete temporary folder
                deleteFolder(temporaryDirectory);
            }
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void addJOIPackage(JOIPackage joiPackage) {
        getJoiPackages().add(joiPackage);
        getJoiPackageLanguages().add(joiPackage.getPackageLanguageCode());
    }

    public JOIPackage getJOIPackage() throws IOException {
        loadJoiPackages();

        String languageCode;
        if(getJoiPackageLanguages().size() != 1) {
            ArrayList<String> languages = new ArrayList<>(getJoiPackageLanguages());
            Collections.sort(languages);
            languageCode = DialogRequestLanguage.requestLanguage(languages);
            if (languageCode == null) return null;
        } else {
            languageCode = getJoiPackageLanguages().iterator().next();
        }

        return getJOIPackage(languageCode);
    }

    public JOIPackage getJOIPackage(String languageCode) {
        for (JOIPackage joiPackage : getJoiPackages())
            if (joiPackage.getPackageLanguageCode().equals(languageCode)) return joiPackage;

        return null;
    }

    public JOIPackage getNewJOIPackage(String languageCode) {
        JOIPackage joiPackage = new JOIPackage();
        joiPackage.setPackageLanguageCode(languageCode);
        joiPackage.setJoi(new JOI());

        MetaData metaData = new MetaData();
        metaData.setName(getJoiPackageDirectory().getName());
        joiPackage.setMetaData(metaData);

        addJOIPackage(joiPackage);

        return joiPackage;
    }

    private JOI importJoiFromDirectory(String languageCode) throws IOException {
        //Initiate loading for joi
        File joiFile = new File(joiPackageDirectory, String.format("joi_text_%s.json", languageCode));

        JSONObject jsonObject = AsisUtils.readJsonFromFile(joiFile);
        if (jsonObject != null) {
            return JOI.createEntity(jsonObject);
        } else
            AsisUtils.errorDialogWindow(new NullPointerException(String.format("Null value was received when reading joi from joi_text_%s.json", languageCode)));

        return null;
    }

    private MetaData importMetaDataFromDirectory(String languageCode) throws IOException {
        //Initiate loading for metaData
        MetaData metaData = new MetaData();
        File metaDataFile = new File(joiPackageDirectory, String.format("info_%s.json", languageCode));

        if (metaDataFile.exists()) {
            //Set meta data icon
            File metaDataIcon = new File(joiPackageDirectory, "joi_icon.png");
            if (metaDataIcon.exists()) metaData.setJoiIcon(metaDataIcon);

            //Tell metadata to load variables from json
            JSONObject jsonObject = AsisUtils.readJsonFromFile(metaDataFile);
            if (jsonObject != null)
                metaData.setDataFromJson(jsonObject.getJSONArray("JOI METADATA").getJSONObject(0));
            else
                AsisUtils.errorDialogWindow(new NullPointerException(String.format("Null value was received when reading metadata from info_%s.json", languageCode)));
        }

        return metaData;
    }

    //Getters and setters
    public File getJoiPackageDirectory() {
        return joiPackageDirectory;
    }

    public void setJoiPackageDirectory(File joiPackageDirectory) {
        this.joiPackageDirectory = joiPackageDirectory;
        if(!joiPackageDirectory.exists())
            //noinspection ResultOfMethodCallIgnored
            joiPackageDirectory.mkdir();
    }

    public Set<String> getJoiPackageLanguages() {
        return joiPackageLanguages;
    }

    public ArrayList<JOIPackage> getJoiPackages() {
        return joiPackages;
    }
}
