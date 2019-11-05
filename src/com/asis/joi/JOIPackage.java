package com.asis.joi;

import com.asis.joi.components.Scene;
import com.asis.utilities.AsisUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class JOIPackage {
    private String packageLanguageCode = "en";
    private JOI joi = new JOI();
    private MetaData metaData = new MetaData();

    //TODO add import method to create full joi and metadata objects
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
