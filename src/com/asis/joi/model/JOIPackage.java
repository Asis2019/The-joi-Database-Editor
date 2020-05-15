package com.asis.joi.model;

import com.asis.controllers.dialogs.DialogMessage;
import com.asis.joi.model.entites.Scene;
import com.asis.joi.model.entites.SceneImage;
import com.asis.utilities.AsisUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class JOIPackage implements Cloneable {
    private String packageLanguageCode;
    private JOI joi;
    private MetaData metaData;

    public void exportPackageAsFiles(File exportDirectory) {
        try {
            //Export json files to export directory
            AsisUtils.writeStringToFile(getMetaData().toJSONString(), String.format("info_%s.json", getPackageLanguageCode()), exportDirectory);
            AsisUtils.writeStringToFile(getJoi().toJSONString(), String.format("joi_text_%s.json", getPackageLanguageCode()), exportDirectory);

            //Copy joi images to export directory
            for (Scene scene : getJoi().getSceneArrayList()) {
                if(scene.hasComponent(SceneImage.class)) {
                    File imageFile = getJoi().getScene(scene.getSceneId()).getComponent(SceneImage.class).getImage();
                    if (imageFile != null) {
                        if (imageFile.exists()) {
                            Files.copy(imageFile.toPath(), exportDirectory.toPath().resolve(imageFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            DialogMessage.messageDialog("WARNING",
                                    String.format("Scene image: %s could not be found at %s and will be skipped.",
                                            scene.getComponent(SceneImage.class).getImage().getName(),
                                            scene.getComponent(SceneImage.class).getImage().getAbsolutePath()),
                                    480, 240);
                        }
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
        } catch (Exception e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    @Override
    public JOIPackage clone() throws CloneNotSupportedException {
        JOIPackage joiPackage = (JOIPackage) super.clone();

        joiPackage.setPackageLanguageCode(getPackageLanguageCode());
        joiPackage.setJoi(getJoi()==null?null:getJoi().clone());
        joiPackage.setMetaData(getMetaData()==null?null:getMetaData().clone());

        return joiPackage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JOIPackage)) return false;

        JOIPackage that = (JOIPackage) o;

        if (getPackageLanguageCode() != null ? !getPackageLanguageCode().equals(that.getPackageLanguageCode()) : that.getPackageLanguageCode() != null)
            return false;
        if (getJoi() != null ? !getJoi().equals(that.getJoi()) : that.getJoi() != null) return false;
        return getMetaData() != null ? getMetaData().equals(that.getMetaData()) : that.getMetaData() == null;
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
