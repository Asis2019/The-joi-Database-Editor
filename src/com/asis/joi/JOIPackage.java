package com.asis.joi;

import com.asis.joi.components.Scene;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.asis.utilities.AsisUtils.deleteFolder;

public class JOIPackage {
    private File packageDirectory;
    private String packageLanguageCode = "en";
    private JOI joi = new JOI();
    private MetaData metaData = new MetaData();

    public JOIPackage() {
        this(new File("defaultWorkspace"));
    }
    public JOIPackage(File packageDirectory) {
        if(!packageDirectory.exists())
            //noinspection ResultOfMethodCallIgnored
            packageDirectory.mkdir();

        setPackageDirectory(packageDirectory);
    }

    public boolean exportPackageAsFiles(File exportDirectory) {
        try {
            //Export json files to export directory
            AsisUtils.writeJsonToFile(getMetaData().getMetaDataAsJson(), String.format("info_%s.json", getPackageLanguageCode()), exportDirectory);
            AsisUtils.writeJsonToFile(getJoi().getJOIAsJson(), String.format("joi_text_%s.json", getPackageLanguageCode()), exportDirectory);

            //Copy joi images to export directory
            for (Scene scene : getJoi().getSceneArrayList()) {
                File imageFile = getJoi().getScene(scene.getSceneId()).getSceneImage();
                if(imageFile != null) Files.copy(imageFile.toPath(), exportDirectory.toPath().resolve(imageFile.getName()), StandardCopyOption.REPLACE_EXISTING);
            }

            //Copy icon to export directory and rename it appropriately
            if(getMetaData().getJoiIcon() != null) {
                File metaDataIcon = getMetaData().getJoiIcon();
                Files.copy(metaDataIcon.toPath(), exportDirectory.toPath().resolve(metaDataIcon.getName()), StandardCopyOption.REPLACE_EXISTING);

                if (!metaDataIcon.getName().equals("joi_icon.png")) {
                    Files.deleteIfExists(new File(exportDirectory.toString() + "/" + "joi_icon.png").toPath());
                    AsisUtils.renameFile(new File(exportDirectory.toPath() + "/" + metaDataIcon.getName()), "joi_icon.png");
                }
            } else {
                InputStream in = getClass().getResourceAsStream("/resources/images/icon_dev.png");
                Path path = Paths.get(exportDirectory.toPath()+"/joi_icon.png");
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }

            //Export completed successfully
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //AsisUtils.errorDialogWindow(e);
            return false;
        }
    }

    public boolean exportPackageAsZip(File zipFile) {
        try {
            //Create temporary folder
            File temporaryDirectory = new File(zipFile.getParent() + "\\tmp");

            //Delete contents of/and folder if present
            if (temporaryDirectory.isDirectory()) AsisUtils.deleteFolder(temporaryDirectory);

            //Make folder and begin process
            if (temporaryDirectory.mkdir()) {
                //Do normal export to temporary directory
                exportPackageAsFiles(temporaryDirectory);

                //Compress temporary folder to zip file
                AsisUtils.writeDirectoryToZip(temporaryDirectory, zipFile);

                //Delete temporary folder
                deleteFolder(temporaryDirectory);
            }

            //Process completed successfully;
            return true;
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
            return false;
        }
    }

    public boolean importPackageFromDirectory(File importDirectory) throws RuntimeException {
        //set package directory to importLocation
        setPackageDirectory(importDirectory);

        try {
            //Start loading chain for joi
            File joiFile = getFileThatExists(importDirectory, "joi_text_%s.json");
            if(joiFile != null) {
                getJoi().setDataFromJson(AsisUtils.readJsonFromFile(joiFile), importDirectory);
            } else {
                throw new RuntimeException("No joi file was found in the selected folder");
            }

            //Initiate loading for metaData
            File metaDataFile = new File(importDirectory, String.format("info_%s.json",getPackageLanguageCode()));
            if(metaDataFile.exists()) {
                //Set meta data icon
                File metaDataIcon = new File(importDirectory, "joi_icon.png");
                if(metaDataIcon.exists()) getMetaData().setJoiIcon(metaDataIcon);

                //Tell metadata to load variables from json
                getMetaData().setDataFromJson(AsisUtils.readJsonFromFile(metaDataFile).getJSONArray("JOI METADATA").getJSONObject(0), importDirectory);
            }

            return true;
        } catch (NullPointerException e) {
            AsisUtils.errorDialogWindow(e);
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Loading failed do to an IOException:\n"+e.getMessage());
        }
    }

    private File getFileThatExists(File importDirectory, String fileName) {
        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            JSONArray jsonArray = ((JSONArray) data);
            for(int i=0; i<jsonArray.length(); i++) {
                final String fileCode = jsonArray.getJSONObject(i).getString("file_code");
                File file = new File(importDirectory, String.format(fileName, fileCode));
                if(file.exists()) {
                    setPackageLanguageCode(fileCode);
                    return file;
                }
            }
        }
        return null;
    }

    //Getters and Setters
    public MetaData getMetaData() {
        return metaData;
    }
    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public JOI getJoi() {
        return joi;
    }
    public void setJoi(JOI joi) {
        this.joi = joi;
    }

    public String getPackageLanguageCode() {
        return packageLanguageCode;
    }
    public void setPackageLanguageCode(String packageLanguageCode) {
        this.packageLanguageCode = packageLanguageCode;
    }

    public File getPackageDirectory() {
        return packageDirectory;
    }

    public void setPackageDirectory(File packageDirectory) {
        this.packageDirectory = packageDirectory;
    }
}
