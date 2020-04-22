package com.asis.joi.model;

import com.asis.joi.model.components.Scene;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class JOIPackage {
    private String packageLanguageCode;
    private JOI joi;
    private MetaData metaData;

    public JOIPackage() {
    }

    public boolean exportPackageAsFiles(File exportDirectory) {
        try {
            //Export json files to export directory
            AsisUtils.writeJsonToFile(getMetaData().getMetaDataAsJson(), String.format("info_%s.json", getPackageLanguageCode()), exportDirectory);
            AsisUtils.writeJsonToFile(getJoi().getJOIAsJson(), String.format("joi_text_%s.json", getPackageLanguageCode()), exportDirectory);

            //Copy joi images to export directory
            for (Scene scene : getJoi().getSceneArrayList()) {
                File imageFile = getJoi().getScene(scene.getSceneId()).getSceneImage();
                if(imageFile != null) {
                    if(imageFile.exists()) {
                        Files.copy(imageFile.toPath(), exportDirectory.toPath().resolve(imageFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Alerts.messageDialog("WARNING",
                                String.format("Scene image: %s could not be found at %s and will be skipped.",
                                    scene.getSceneImage().getName(),
                                    scene.getSceneImage().getAbsolutePath()),
                                480, 240);
                    }
                }
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
            AsisUtils.errorDialogWindow(e);
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
