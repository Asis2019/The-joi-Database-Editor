package com.asis.utilities;

import com.asis.controllers.Controller;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
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
        Alerts.messageDialog("Error", "Oh no an error! Send it to Asis so he can feel bad.\n"+e.getMessage());
    }

    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        addAllDescendants(root, nodes);
        return nodes;
    }

    private static void addAllDescendants(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendants((Parent)node, nodes);
        }
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1, file.getName().length());
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
        FileWriter fileWriter = new FileWriter(saveLocation.toPath() + File.separator + fileName);
        fileWriter.write(jsonObject.toString(4));
        fileWriter.flush();
        fileWriter.close();
    }

    public static JSONObject readJsonFromFile(File file) {
        try {
            String text = new String(Files.readAllBytes(file.toPath()));
            return new JSONObject(text);
        } catch (IOException | JSONException e) {
            errorDialogWindow(e);
        }
        return null;
}

    public static void writeDirectoryToZip(File dataDirectory, File zipFile) throws IOException {
        byte[] buffer = new byte[1024];
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        File[] files = dataDirectory.listFiles();

        if (files != null) {
            for (File value : files) {
                FileInputStream fis = new FileInputStream(value);
                zos.putNextEntry(new ZipEntry(value.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
        }
        zos.close();
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
}