package com.asis.controllers;

import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.components.Line;
import com.asis.joi.model.components.Scene;
import com.asis.utilities.AsisUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;

public class TranslationEditor {

    @FXML
    private TableView<TableRow> tableView;

    public void initialize() {
        initTable();
        loadData();
    }

    private void initTable() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        initColumns();

        tableView.setEditable(true);
    }

    private void initColumns() {
        ObservableList<String> joiPackageLanguages = JOIPackageManager.getInstance().getJoiPackageLanguages();
        for (int i = 0; i < joiPackageLanguages.size(); i++) {
            String languageCode = joiPackageLanguages.get(i);

            TableColumn<TableRow, String> columnLineText = new TableColumn<>(AsisUtils.getLanguageNameForCode(languageCode));
            columnLineText.setSortable(false);

            final int finalI = i;
            columnLineText.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getRowData().get(finalI).getText()));

            editableColumns(columnLineText, i);
        }
    }

    private void editableColumns(TableColumn<TableRow, String> tableColumn, final int dataIndex) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableColumn.setOnEditCommit(e-> e.getTableView().getItems().get(e.getTablePosition().getRow()).getRowData().get(dataIndex).setText(e.getNewValue()));

        tableView.getColumns().add(tableColumn);
    }

    private void loadData() {
        ObservableList<TableRow> itemsList = FXCollections.observableArrayList();

        ArrayList<String> unloadedLanguages = new ArrayList<>(JOIPackageManager.getInstance().getJoiPackageLanguages());
        JOIPackageManager.getInstance().getJoiPackages().forEach(i -> unloadedLanguages.remove(i.getPackageLanguageCode()));
        unloadedLanguages.forEach(i -> {
            try {
                JOIPackageManager.getInstance().getJOIPackage(i);
            } catch (IOException e) {
                e.printStackTrace();
                AsisUtils.errorDialogWindow(e);
            }
        });

        ArrayList<JOIPackage> joiPackages = JOIPackageManager.getInstance().getJoiPackages();
        for (int i = 0; i < joiPackages.size(); i++) {
            JOIPackage joiPackage = joiPackages.get(i);

            int rowIndex = 0;
            for (Scene scene : joiPackage.getJoi().getSceneArrayList()) {

                for(Line line: scene.getLineArrayList()) {
                    addLineToRow(itemsList, i, rowIndex, line);
                    rowIndex++;
                }

                if (scene.getTimer() != null) {
                    for(Line line: scene.getTimer().getLineArrayList()) {
                        addLineToRow(itemsList, i, rowIndex, line);
                        rowIndex++;
                    }
                }
            }
        }

        tableView.setItems(itemsList);
    }

    private void addLineToRow(ObservableList<TableRow> itemsList, int i, int rowIndex, Line line) {
        TableRow tableRow;
        if(itemsList.size()-1 >= rowIndex) {
            tableRow = itemsList.get(rowIndex);
            tableRow.getRowData().add(i, line);
        } else {
            tableRow = new TableRow();tableRow.getRowData().add(i, line);
            itemsList.add(tableRow);
        }
    }

    public void actionAddLanguage() {
        try {
            String newLanguage = DialogRequestLanguage.requestLanguage(true);

            JOIPackage joiPackage = JOIPackageManager.getInstance().getJOIPackage(Controller.getInstance().getJoiPackage().getPackageLanguageCode());
            joiPackage.setPackageLanguageCode(newLanguage);

            JOIPackageManager.getInstance().addJOIPackage(joiPackage);

            //Add to table
            initTable();
        } catch (IOException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionClose() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private static class TableRow {
        ObservableList<Line> rowData = FXCollections.observableArrayList();

        public ObservableList<Line> getRowData() {
            return rowData;
        }
    }
}
