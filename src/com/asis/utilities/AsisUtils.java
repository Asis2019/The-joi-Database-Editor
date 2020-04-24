package com.asis.utilities;

import com.asis.controllers.Controller;
import com.asis.joi.JOIPackageManager;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
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

    private static String colorChanelToHex(double chanelValue) {
        String rtn = Integer.toHexString((int) Math.min(Math.round(chanelValue * 255), 255));
        if (rtn.length() == 1) {
            rtn = "0" + rtn;
        }
        return rtn;
    }

    public static void errorDialogWindow(Exception e) {
        e.printStackTrace();
        Alerts.messageDialog("Error", "Oh no an error! Send it to Asis so he can feel bad.\n"+e.getMessage());
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static boolean renameFile(File fileToRename, String newName) {
        // This method will rename a File that is passed in
        String oldFileName = fileToRename.getName();
        String oldFilePath = fileToRename.getPath();
        String newPath = oldFilePath.replace(oldFileName, newName);
        return fileToRename.renameTo(new File(newPath));
    }

    public static void writeJsonToFile(JSONObject jsonObject, String fileName, File saveLocation) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveLocation.toPath() + File.separator + fileName), StandardCharsets.UTF_8))) {
            bufferedWriter.write(jsonObject.toString(4));
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
        if(files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    if(!f.delete()) {
                        System.out.println("Failed to delete file: "+f.getPath());
                    }
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
            while ((message = reader.readLine()) != null) {
                stringBuilder.append(message).append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return "An error occurred while getting text file \n"+e.getMessage();
        }
    }

    public static ArrayList<Integer> convertJSONArrayToList(JSONArray jsonArray) {
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<jsonArray.length(); i++) {
            list.add(jsonArray.getInt(i));
        }
        return list;
    }

    public static String getLanguageCodeForName(String name) {
        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            for(int i=0; i<((JSONArray) data).length(); i++) {
                if(((JSONArray) data).getJSONObject(i).getString("menu_name").equals(name)) {
                    return ((JSONArray) data).getJSONObject(i).getString("file_code");
                }
            }
        }
        throw new IllegalArgumentException(String.format("The language code for %s could not be found in settings.json\nPlease make sure to select a valid option from the dropdown.", name));
    }

    public static String getLanguageNameForCode(String name) {
        Object data = Config.get("LANGUAGES");
        if(data instanceof JSONArray) {
            for(int i=0; i<((JSONArray) data).length(); i++) {
                if(((JSONArray) data).getJSONObject(i).getString("file_code").equals(name)) {
                    return ((JSONArray) data).getJSONObject(i).getString("menu_name");
                }
            }
        }
        throw new IllegalArgumentException(String.format("The language code for %s could not be found in settings.json\nPlease make sure to select a valid option from the dropdown.", name));
    }

    public static File imageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpg", "*.jpg"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("jpeg", "*.jpeg"));

        return fileChooser.showOpenDialog(null);
    }
}