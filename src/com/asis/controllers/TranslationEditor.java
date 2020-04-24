package com.asis.controllers;

import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.components.Line;
import com.asis.joi.model.components.Scene;
import com.asis.utilities.Alerts;
import com.asis.utilities.AsisUtils;
import com.asis.utilities.Config;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

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
            columnLineText.setPrefWidth(300d);

            final int finalI = i;
            columnLineText.setCellValueFactory(param -> {
                ReadOnlyObjectWrapper<String> readOnlyObjectWrapper;
                if(param.getValue().getRowData().size()-1 >= finalI) {
                    readOnlyObjectWrapper = new ReadOnlyObjectWrapper<>(param.getValue().getRowData().get(finalI).getText());
                } else {
                    readOnlyObjectWrapper = new ReadOnlyObjectWrapper<>();
                }

                return readOnlyObjectWrapper;
            });

            editableColumns(columnLineText, i);
        }
    }

    private void editableColumns(TableColumn<TableRow, String> tableColumn, final int dataIndex) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableColumn.setOnEditCommit(e-> {
            TableRow tableRow = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(tableRow.getRowData().size()-1 >= dataIndex) {
                tableRow.getRowData().get(dataIndex).setText(e.getNewValue());
            } else {
                Alerts.messageDialog("Error", "The field was unable to update, because the line/scene data structure is missing in the joi_text file." +
                        " Please make sure all joi_text files have the same structure before attempting to translate.\n");
                e.consume();
            }
        });

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
        joiPackages.sort(Comparator.comparing(JOIPackage::getPackageLanguageCode));
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
            ArrayList<String> languages = new ArrayList<>();
            Object data = Config.get("LANGUAGES");
            if(data instanceof JSONArray) {
                for(int i=0; i<((JSONArray) data).length(); i++) {
                    String languageCode = ((JSONArray) data).getJSONObject(i).getString("file_code");
                    if(!JOIPackageManager.getInstance().getJoiPackageLanguages().contains(languageCode))
                        languages.add(languageCode);
                }
            }

            String newLanguage = DialogRequestLanguage.requestLanguage(languages);
            if(newLanguage == null) return;

            JOIPackage joiPackage = JOIPackageManager.getInstance().cloneJOIPackage(Controller.getInstance().getJoiPackage());
            joiPackage.setPackageLanguageCode(newLanguage);

            JOIPackageManager.getInstance().addJOIPackage(joiPackage);

            //Add to table
            initTable();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
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
