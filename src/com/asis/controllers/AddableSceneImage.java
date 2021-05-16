package com.asis.controllers;

import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.entities.Scene;
import com.asis.joi.model.entities.SceneImage;
import com.asis.ui.ImageViewPane;
import com.asis.utilities.GifDecoder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.asis.utilities.AsisUtils.errorDialogWindow;

public interface AddableSceneImage {

    default boolean createNewSceneImage(File file, Scene scene) {
        SceneImage image = new SceneImage();

        try {
            // Check if file is a gif
            if (file.getName().endsWith(".gif")) {
                File spriteSheetFile = new File(JOIPackageManager.getInstance().getJoiPackageDirectory() +
                        File.separator +
                        file.getName().replace(".gif", ".png"));

                BufferedImage spriteSheet = convertGifToSpriteSheet(file);
                ImageIO.write(spriteSheet, "png", spriteSheetFile);

                image.setFrames(getGifFrameRate(file));
                double value = (double) image.getFrames() / ((double) getDelays(file) / 100d);

                image.setFrameRate(value / 60d);
                image.setImage(spriteSheetFile);
            } else image.setImage(file);

            scene.addComponent(image);

            return true;
        } catch (IOException e) {
            errorDialogWindow(e);
        }

        return false;
    }

    static int getDelays(File file) throws IOException {
        FileInputStream data = new FileInputStream(file);
        GifDecoder.GifImage gif = GifDecoder.read(data);
        int n = gif.getFrameCount();
        int totalDelay = 0;
        for (int i = 0; i < n; i++) {
            totalDelay += gif.getDelay(i);
        }
        return totalDelay;
    }

    /**
     * Converts the passed in file, to a 1 row multi column sprite sheet.
     *
     * @param gifFile - The file to be converted
     * @return BufferedImage - the final stitched image
     */
    static BufferedImage convertGifToSpriteSheet(File gifFile) throws IOException {
        FileInputStream data = new FileInputStream(gifFile);

        GifDecoder.GifImage gif = GifDecoder.read(data);
        int frameCount = gif.getFrameCount();

        BufferedImage finalImage = gif.getFrame(0);

        for (int i = 1; i < frameCount; i++) {
            final BufferedImage img = gif.getFrame(i);

            int width = finalImage.getWidth() + img.getWidth();
            int height = Math.max(finalImage.getHeight(), img.getHeight());

            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = newImage.createGraphics();
            java.awt.Color oldColor = g2.getColor();
            g2.setPaint(java.awt.Color.BLACK);
            g2.fillRect(0, 0, width, height);
            g2.setColor(oldColor);
            g2.drawImage(finalImage, null, 0, 0);
            g2.drawImage(img, null, finalImage.getWidth(), 0);
            g2.dispose();

            finalImage = newImage;
        }

        return finalImage;
    }

    /**
     * Returns the number of frames the gif file contains
     * @param gifFile - the gif file
     * @return int - the number of frames in the gif
     */
    static int getGifFrameRate(File gifFile) throws IOException {
        FileInputStream data = new FileInputStream(gifFile);

        GifDecoder.GifImage gif = GifDecoder.read(data);
        return gif.getFrameCount();
    }

    default void setVisibleImage(EditorWindow editorWindow, StackPane stackPane, ImageViewPane viewPane, File workingFile) {
        if (viewPane.getImageFile() != workingFile) {
            //Remove image if any is present
            stackPane.getChildren().remove(viewPane);

            //Make image visible
            Image image = new Image(workingFile.toURI().toString());
            ImageView sceneImageView = new ImageView();
            sceneImageView.setImage(image);
            sceneImageView.setPreserveRatio(true);
            viewPane.setImageView(sceneImageView);
            viewPane.setImageFile(workingFile);
            stackPane.getChildren().add(0, viewPane);

            if (editorWindow.isShowThumbnail()) editorWindow.toggleSceneThumbnails(true);
        }
    }

}
