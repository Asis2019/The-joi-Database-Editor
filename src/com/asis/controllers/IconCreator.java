package com.asis.controllers;

import com.asis.controllers.dialogs.DialogCropImage;
import com.asis.joi.JOIPackageManager;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class IconCreator {

    @FXML
    private ImageView iconImageView;
    @FXML
    private ColorPicker borderPicker, fillPicker;

    private static Image characterImage;
    private static Image finalImage;

    public void initialize() {
        ChangeListener<Color> changeListener = ((observable, oldValue, newValue) -> {
            try {
                createAndDisplayIcon();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        borderPicker.valueProperty().addListener(changeListener);
        fillPicker.valueProperty().addListener(changeListener);

        try {
            createAndDisplayIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void actionAddCharacterImage() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            characterImage = DialogCropImage.openImageCrop(file);
            createAndDisplayIcon();
        }
    }

    @FXML
    public void actionSaveIcon() throws IOException {
        finalImage = iconImageView.getImage();

        ImageIO.write(SwingFXUtils.fromFXImage(finalImage, null), "png",
                new File(JOIPackageManager.getInstance().getJoiPackageDirectory(),"joi_icon.png"));

        Stage stage = (Stage) iconImageView.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void actionUseFile() throws IOException {FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Icon");
        fileChooser.setInitialDirectory(JOIPackageManager.getInstance().getJoiPackageDirectory());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png", "*.png"));
        File file = fileChooser.showOpenDialog(new Stage());

        if(file != null) {
            Image image = new Image(file.toURI().toString());
            iconImageView.setImage(image);
            actionSaveIcon();
        }
    }

    @FXML
    public void actionCancel() {
        finalImage = null;
        Stage stage = (Stage) iconImageView.getScene().getWindow();
        stage.close();
    }

    public static Image show() {
        try {

            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(IconCreator.class.getResource("/resources/fxml/icon_creator.fxml"));
            Parent root = fxmlLoader.load();

            Scene main_scene = new Scene(root, 400, 580);

            stage.setResizable(true);
            stage.getIcons().add(new Image(Controller.class.getResourceAsStream("/resources/images/icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(main_scene);
            stage.setTitle("Crop Image");

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalImage;
    }

    private void createAndDisplayIcon() throws IOException {
        BufferedImage newImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = newImage.createGraphics();
        java.awt.Color oldColor = g2.getColor();


        // Set background fill
        g2.setPaint(convertImageFormats(fillPicker.getValue()));
        g2.fill(new RoundRectangle2D.Float(0, 0, 300, 300, 50, 50));
        g2.setColor(oldColor);

        // Draw Character
        if (characterImage != null) {
            g2.setClip(new RoundRectangle2D.Double(0, 0, 300, 300, 50, 50));

            double x = (300 - characterImage.getWidth()) / 2;
            double y = (300 - characterImage.getHeight()) / 2;
            g2.drawImage(SwingFXUtils.fromFXImage(characterImage,
                    new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)),
                    (int) x, (int) y, null);
            g2.setClip(null);
        }

        // Draw border
        BufferedImage border = ImageIO.read(getClass().getResourceAsStream("/resources/images/icon_template.png"));
        tint(border, convertImageFormats(borderPicker.getValue()));
        g2.drawImage(border, null, 0, 0);


        g2.dispose();

        iconImageView.setImage(SwingFXUtils.toFXImage(newImage, null));
    }

    private static java.awt.Color convertImageFormats(javafx.scene.paint.Color color) {
        return new java.awt.Color((float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue(),
                (float) color.getOpacity());
    }

    private static void tint(BufferedImage image, java.awt.Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                java.awt.Color pixelColor = new java.awt.Color(image.getRGB(x, y), true);
                int r = (pixelColor.getRed() + color.getRed()) / 2;
                int g = (pixelColor.getGreen() + color.getGreen()) / 2;
                int b = (pixelColor.getBlue() + color.getBlue()) / 2;
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }
}
