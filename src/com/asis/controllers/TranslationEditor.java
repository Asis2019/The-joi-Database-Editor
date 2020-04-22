package com.asis.controllers;

import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.components.Line;
import com.asis.joi.model.components.Scene;
import com.asis.utilities.AsisUtils;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class TranslationEditor {

    @FXML
    private TableView<Line> tableView;

    public void initialize() {
        initTable();
        loadData();
    }

    private void initTable() {
        initColumns();
    }

    private void initColumns() {
        for(String languageCode: JOIPackageManager.getInstance().getJoiPackageLanguages()) {
            TableColumn<Line, String> columnLineText = new TableColumn<>(AsisUtils.getLanguageNameForCode(languageCode));
            columnLineText.setCellValueFactory(new PropertyValueFactory<>("text"));

            editableColumns(columnLineText);
        }

        JOIPackageManager.getInstance().getJoiPackageLanguages().addListener((ListChangeListener<String>) change -> {
            change.next();
            TableColumn<Line, String> columnLineText = new TableColumn<>(AsisUtils.getLanguageNameForCode(change.getAddedSubList().get(0)));
            columnLineText.setCellValueFactory(new PropertyValueFactory<>("text"));

            editableColumns(columnLineText);
        });
    }

    private void editableColumns(TableColumn<Line, String> tableColumn) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableColumn.setOnEditCommit(e-> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setText(e.getNewValue());
            System.out.println("Table commit");
        });
        tableColumn.setOnEditCancel(e->System.out.println("Table cancel"));
        tableColumn.setOnEditStart(e->System.out.println("Table start"));

        tableView.getColumns().add(tableColumn);
        tableView.setEditable(true);
    }

    private void loadData() {
        ObservableList<Line> itemsList = FXCollections.observableArrayList();

        for(JOIPackage joiPackage: JOIPackageManager.getInstance().getJoiPackages()) {
            for(Scene scene: joiPackage.getJoi().getSceneArrayList()) {
                itemsList.addAll(scene.getLineArrayList());
                if(scene.getTimer() != null) itemsList.addAll(scene.getTimer().getLineArrayList());
            }
        }

        tableView.setItems(itemsList);
    }

    public void actionAddLanguage() {
        try {
            String newLanguage = DialogRequestLanguage.requestLanguage(true);

            JOIPackage joiPackage = JOIPackageManager.getInstance().getJOIPackage(Controller.getInstance().getJoiPackage().getPackageLanguageCode());
            joiPackage.setPackageLanguageCode(newLanguage);

            JOIPackageManager.getInstance().addJOIPackage(joiPackage);
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionClose() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
