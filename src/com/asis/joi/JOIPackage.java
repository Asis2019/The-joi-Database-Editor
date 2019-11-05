package com.asis.joi;

import com.asis.joi.components.Scene;
import com.asis.utilities.AsisUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.asis.utilities.AsisUtils.deleteFolder;

public class JOIPackage {
    private String packageLanguageCode = "en";
    private JOI joi = new JOI();
    private MetaData metaData = new MetaData();
    
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
        try {
            //Start loading chain for joi
            File joiFile = new File(importDirectory, String.format("joi_text_%s.json", getPackageLanguageCode()));
            if(joiFile.exists()) {
                getJoi().setDataFromJson(AsisUtils.readJsonFromFile(joiFile));
            }

            //Initiate loading for metaData
            File metaDataFile = new File(importDirectory, String.format("info_%s.json", getPackageLanguageCode()));
            if(metaDataFile.exists()) {
                //Set meta data icon
                File meteDataIcon = new File(importDirectory, "joi_icon.png");
                if(meteDataIcon.exists()) getMetaData().setJoiIcon(meteDataIcon);

                //Tell metadata to load variables from json
                getMetaData().setDataFromJson(AsisUtils.readJsonFromFile(metaDataFile).getJSONArray("JOI METADATA").getJSONObject(0));
            }

            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
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
}
