package com.asis.controllers.dialogs;

import com.asis.Main;
import com.asis.joi.model.entities.Arithmetic;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import static com.asis.utilities.AsisUtils.getDefaultTitle;

public class DialogArithmetic {

    @FXML
    private TextField arithmeticTitle, mathematicalExpression;

    private Arithmetic arithmetic;

    private static boolean result = true;

    public void init() {
        arithmeticTitle.setText(getDefaultTitle(arithmetic, "Arithmetic"));
        mathematicalExpression.setText(arithmetic.getMathematicalExpression());

        //TODO implement auto complete for variables
    }

    public void actionSave() {
        try {
            arithmetic.setComponentTitle(arithmeticTitle.getText().trim());
            arithmetic.setMathematicalExpression(mathematicalExpression.getText().trim());

            Stage stage = (Stage) arithmeticTitle.getScene().getWindow();
            StageManager.getInstance().closeStage(stage);
        } catch (NullPointerException e) {
            DialogMessage.messageDialog("Unable to save arithmetic", "An unknown error has occurred | NullPointer", 600, 200);
        }
    }

    public void setArithmetic(Arithmetic arithmetic) {
        this.arithmetic = arithmetic;
    }

    public static boolean openArithmetic(Arithmetic arithmetic) {
        result = true;

        try {
            Stage stage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/dialog_arithmetic.fxml"));
            Parent root = fxmlLoader.load();

            DialogArithmetic controller = fxmlLoader.getController();
            controller.setArithmetic(arithmetic);
            controller.init();

            Scene main_scene = new Scene(root);

            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
            stage.setScene(main_scene);
            stage.setUserData(arithmetic.getComponentId());
            stage.setTitle("Arithmetic");
            stage.setOnCloseRequest(windowEvent -> result = false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }

        return result;
    }

}
