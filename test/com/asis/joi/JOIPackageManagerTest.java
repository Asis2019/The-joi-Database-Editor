package com.asis.joi;

import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JOIPackageManagerTest {

    @Test
    public void testLoadJoiPackageLanguages() {
        //Method loadJoiPackageLanguages get's called when the package directory is set
        JOIPackageManager.getInstance().setJoiPackageDirectory(new File("defaultWorkspace/gloria"));
        assertEquals(FXCollections.observableArrayList("en"), JOIPackageManager.getInstance().getJoiPackageLanguages());
    }


}
