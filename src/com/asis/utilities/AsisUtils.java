package com.asis.utilities;

import com.asis.controllers.Controller;
import com.asis.controllers.dialogs.DialogMessage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.entities.JOIComponent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AsisUtils {

    public static String colorToHex(Color color) {
        return colorChanelToHex(color.getRed())
                + colorChanelToHex(color.getGreen())
                + colorChanelToHex(color.getBlue())
                + colorChanelToHex(color.getOpacity());
    }

    private static String colorChanelToHex(double channelValue) {
        String rtn = Integer.toHexString((int) Math.min(Math.round(channelValue * 255), 255));
        if (rtn.length() == 1) {
            rtn = "0" + rtn;
        }
        return rtn;
    }

    public static void errorDialogWindow(Exception e) {
        e.printStackTrace();
        DialogMessage.messageDialog("Error", "Oh no an error! Send it to Asis so he can feel bad.\n" + e.getMessage());
    }

    public static boolean renameFile(File fileToRename, String newName) {
        // This method will renameNode a File that is passed in
        String oldFileName = fileToRename.getName();
        String oldFilePath = fileToRename.getPath();
        String newPath = oldFilePath.replace(oldFileName, newName);
        return fileToRename.renameTo(new File(newPath));
    }

    public static void writeStringToFile(String string, String fileName, File saveLocation) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveLocation.toPath() + File.separator + fileName), StandardCharsets.UTF_8))) {
            bufferedWriter.write(string);
        }
    }

    public static JSONObject readJsonFromFile(File file) throws IOException {
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            String text = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            return new JSONObject(text);
        } catch (JSONException e) {
            errorDialogWindow(e);
        }
        return null;
    }

    public static void writeDirectoryToZip(File dataDirectory, File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        File[] files = dataDirectory.listFiles();

        if (files != null) {
            for (File value : files) {
                FileInputStream fileInputStream = new FileInputStream(value);
                zipOutputStream.putNextEntry(new ZipEntry(value.getName()));
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, length);
                }
                zipOutputStream.closeEntry();
                fileInputStream.close();
            }
        }
        zipOutputStream.close();
    }

    public static boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if (!f.delete()) System.out.println("Failed to delete file: " + f.getPath());
                }
            }
        }

        try {
            Files.delete(folder.toPath());
            return true;
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return false;
    }

    public static String getStringFromFile(String fileLocation) {
        try {
            String message;
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = Controller.class.getResourceAsStream(fileLocation);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((message = reader.readLine()) != null) stringBuilder.append(message).append("\n");
            return stringBuilder.toString();
        } catch (IOException e) {
            return "An error occurred while getting text file \n" + e.getMessage();
        }
    }

    public static ArrayList<Integer> convertJSONArrayToList(JSONArray jsonArray) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) list.add(jsonArray.getInt(i));
        return list;
    }

    public static String getValueForAlternateKey(JSONArray array, String value, String targetKey, String haveKey) {
        for (int i = 0; i < array.length(); i++) {
            if(array.getJSONObject(i).getString(haveKey).equals(value)) {
                return array.getJSONObject(i).getString(targetKey);
            }
        }
        throw new IllegalArgumentException(String.format("The language code for %s could not be found in settings.json\nPlease make sure to select a valid option from the dropdown.", value));
    }

    public static File imageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("gif", "*.gif"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("(dev only) jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("(dev only) jpg", "*.jpeg"));

        return fileChooser.showOpenDialog(null);
    }

    /**
     * Adds the data from the second json object into the first.
     * @param jsonObject1 - JSONObject to merge data into
     * @param jsonObject2 - JSONObject with data to merge
     * @return JSONObject
     */
    public static JSONObject mergeObject(JSONObject jsonObject1, JSONObject jsonObject2) {
        JSONObject merged = new JSONObject(jsonObject1, JSONObject.getNames(jsonObject1));
        for (String key : JSONObject.getNames(jsonObject2)) {
            merged.put(key, jsonObject2.get(key));
        }
        return merged;
    }

    public static String getDefaultTitle(JOIComponent joiComponent, String prefix) {
        if(joiComponent.getComponentTitle() == null) {
            final int sceneId = Controller.getInstance().getJoiPackage().getJoi().getSceneIdCounter();
            return prefix + " " + sceneId;
        } else {
            return joiComponent.getComponentTitle();
        }
    }

    // Hacky code to change tooltips speed
    public static void hackTooltipStartTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Point2D screenToStage(Stage stage, Point2D screenPoint) {
        double x = (screenPoint.getX() - stage.getX());
        double y = (screenPoint.getY() - stage.getY() - (stage.getHeight() - stage.getScene().getHeight()));
        return new Point2D(x, y);
    }
}