package com.asis.joi;

import com.asis.joi.components.Scene;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        if(!packageDirectory.exists()) packageDirectory.mkdir();

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
            File metaDataIcon = new File(this.getClass().getResource("/resources/images/icon_dev.png").getPath());
            if (getMetaData().getJoiIcon() != null) metaDataIcon = getMetaData().getJoiIcon();

            Files.copy(metaDataIcon.toPath(), exportDirectory.toPath().resolve(metaDataIcon.getName()), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(new File(exportDirectory.toString() + "\\" +"joi_icon.png").toPath());
            AsisUtils.renameFile(new File(exportDirectory.toPath() + "\\" + metaDataIcon.getName()), "joi_icon.png");

            //Export completed successfully
            return true;
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
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

    public boolean importPackageFromDirectory(File importDirectory) {
        //set package directory to importLocation
        setPackageDirectory(importDirectory);

        try {
            //Start loading chain for joi
            File joiFile = new File(importDirectory, String.format("joi_text_%s.json", getPackageLanguageCode()));
            if(joiFile.exists()) {
                getJoi().setDataFromJson(AsisUtils.readJsonFromFile(joiFile), importDirectory);
            } else {
                throw new RuntimeException("No joi file was found in the selected folder");
            }

            //Initiate loading for metaData
            File metaDataFile = new File(importDirectory, String.format("info_%s.json", getPackageLanguageCode()));
            if(metaDataFile.exists()) {
                //Set meta data icon
                File metaDataIcon = new File(importDirectory, "joi_icon.png");
                if(metaDataIcon.exists()) getMetaData().setJoiIcon(metaDataIcon);

                //Tell metadata to load variables from json
                getMetaData().setDataFromJson(AsisUtils.readJsonFromFile(metaDataFile).getJSONArray("JOI METADATA").getJSONObject(0), importDirectory);
            }

            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        } catch (RuntimeException e) {
            Alerts.messageDialog("LOADING FAILED", "The editor was unable to load this joi for the following reason:\n"+e.getMessage(), 600, 200);
            return false;
        }
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
