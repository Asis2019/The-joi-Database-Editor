package asis.custom_objects;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import org.controlsfx.dialog.ExceptionDialog;

import java.io.File;
import java.util.ArrayList;

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
        ExceptionDialog exceptionDialog = new ExceptionDialog(e);
        exceptionDialog.setTitle("Error");
        exceptionDialog.setHeaderText("Oh no an error! Send it to Asis so he can feel bad.\n"+e.getMessage());
        exceptionDialog.show();
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

}