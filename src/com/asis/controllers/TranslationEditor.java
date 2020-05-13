package com.asis.controllers;

import com.asis.controllers.dialogs.DialogMessage;
import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entites.*;
import com.asis.joi.model.entites.dialog.Dialog;
import com.asis.joi.model.entites.dialog.DialogOption;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

public class TranslationEditor {

    @FXML
    private TableView<TableRow<SceneComponent<?>>> tableViewJoi;

    public void initialize() {
        initJoiTable();
        loadJoiTableData();
    }

    //Setup joi table
    private void initJoiTable() {
        tableViewJoi.getColumns().clear();
        tableViewJoi.getItems().clear();
        tableViewJoi.getSelectionModel().setCellSelectionEnabled(true);

        initJoiColumns();

        tableViewJoi.setEditable(true);
    }

    private void initJoiColumns() {
        //Add line location column
        TableColumn<TableRow<SceneComponent<?>>, String> lineLocationColumn = new TableColumn<>("Location");
        lineLocationColumn.setSortable(false);
        lineLocationColumn.setPrefWidth(200d);
        lineLocationColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(getTextFromEntity(param.getValue().getData(0))));
        lineLocationColumn.getStyleClass().add("first-column");
        tableViewJoi.getColumns().add(lineLocationColumn);

        //Add language columns
        int i = 1;
        Set<String> joiPackageLanguages = JOIPackageManager.getInstance().getJoiPackageLanguages();
        for (String languageCode : joiPackageLanguages) {
            TableColumn<TableRow<SceneComponent<?>>, String> columnLineText = new TableColumn<>(AsisUtils.getLanguageValueForAlternateKey(languageCode, "file_code"));
            columnLineText.setSortable(false);
            columnLineText.setPrefWidth(300d);

            final int finalI = i;
            columnLineText.setCellValueFactory(param -> {
                ReadOnlyObjectWrapper<String> readOnlyObjectWrapper;
                if (param.getValue().getRowData().size() - 1 >= finalI) {
                    readOnlyObjectWrapper = new ReadOnlyObjectWrapper<>(getTextFromEntity(param.getValue().getData(finalI)));
                } else {
                    readOnlyObjectWrapper = new ReadOnlyObjectWrapper<>();
                }

                return readOnlyObjectWrapper;
            });

            editableJoiColumn(columnLineText, i);
            i++;
        }
    }

    private void editableJoiColumn(TableColumn<TableRow<SceneComponent<?>>, String> tableColumn, final int dataIndex) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableColumn.setOnEditCommit(e-> {
            TableRow<SceneComponent<?>> tableRow = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(tableRow.getRowData().size()-1 >= dataIndex) {
                SceneComponent<?> entity = tableRow.getRowData().get(dataIndex);
                if(entity instanceof Line) ((Line) entity).setText(e.getNewValue());
                else if (entity instanceof DialogOption) ((DialogOption) entity).setOptionText(e.getNewValue());
                else if (entity instanceof Transition) ((Transition) entity).setTransitionText(e.getNewValue());
            } else {
                DialogMessage.messageDialog("Error", "The field was unable to update, because the line/scene data structure is missing in the joi_text file." +
                        " Please make sure all joi_text files have the same structure before attempting to translate.\n");
                e.consume();
            }
        });

        tableViewJoi.getColumns().add(tableColumn);
    }

    private void loadJoiTableData() {
        ObservableList<TableRow<SceneComponent<?>>> itemsList = FXCollections.observableArrayList();

        ArrayList<JOIPackage> joiPackages = JOIPackageManager.getInstance().getJoiPackages();
        joiPackages.sort(Comparator.comparing(JOIPackage::getPackageLanguageCode));
        for (int columnIndex = 1; columnIndex <= joiPackages.size(); columnIndex++) {
            JOIPackage joiPackage = joiPackages.get(columnIndex-1);

            int rowIndex = 0;
            for (Scene scene : joiPackage.getJoi().getSceneArrayList()) {
                //Add normal text
                for(Line line: scene.getComponent(LineGroup.class).getLineArrayList()) {
                    rowIndex = addDataToCell(itemsList, columnIndex, rowIndex, scene.getSceneTitle() + " - Line " + line.getLineNumber(), line);
                }

                //Add text from timers options
                if (scene.hasComponent(Timer.class)) {
                    for(Line line: scene.getComponent(Timer.class).getLineGroup().getLineArrayList()) {
                        rowIndex = addDataToCell(itemsList, columnIndex, rowIndex, scene.getSceneTitle() + " - Timer - Line " + line.getLineNumber(), line);
                    }
                }

                //Add text from dialog options
                if(scene.hasComponent(Dialog.class)) {
                    for(DialogOption dialogOption: scene.getComponent(Dialog.class).getOptionArrayList()) {
                        rowIndex = addDataToCell(itemsList, columnIndex, rowIndex, scene.getSceneTitle() + " - Dialog - Option " + dialogOption.getOptionNumber(), dialogOption);
                    }
                }

                //Add text from transitions
                if(scene.hasComponent(Transition.class) && scene.getComponent(Transition.class).getTransitionText() != null) {
                    rowIndex = addDataToCell(itemsList, columnIndex, rowIndex, scene.getSceneTitle() + " - Transition", scene.getComponent(Transition.class));
                }
            }
        }

        tableViewJoi.setItems(itemsList);
    }

    private int addDataToCell(ObservableList<TableRow<SceneComponent<?>>> itemsList, int columnIndex, int rowIndex, String cellData, SceneComponent<?> entity) {
        if(columnIndex == 1)
            addLineToRow(itemsList, 0, rowIndex, cellData);
        addLineToRow(itemsList, columnIndex, rowIndex, entity);
        rowIndex++;
        return rowIndex;
    }

    private String getTextFromEntity(SceneComponent<?> entity) {
        if(entity instanceof Line) return ((Line) entity).getText();
        else if (entity instanceof DialogOption) return ((DialogOption) entity).getOptionText();
        else if (entity instanceof Transition) return ((Transition) entity).getTransitionText();
        else return null;
    }

    private <T> void addLineToRow(ObservableList<TableRow<SceneComponent<?>>> itemsList, int columnIndex, int rowIndex, T entity) {
        SceneComponent<?> insertionEntity;
        if(entity instanceof SceneComponent<?>) insertionEntity = (SceneComponent<?>) entity;
        else if(entity instanceof String) insertionEntity = Line.createEntity(new JSONObject("{\"text\":\""+ entity + "\"}"));
        else throw new IllegalArgumentException("Cell data value must be a String or and object that implements SceneComponent");

        TableRow<SceneComponent<?>> tableRow;
        if(itemsList.size()-1 >= rowIndex) {
            tableRow = itemsList.get(rowIndex);
            tableRow.getRowData().add(columnIndex, insertionEntity);
        } else {
            tableRow = new TableRow<>();
            tableRow.getRowData().add(columnIndex, insertionEntity);
            itemsList.add(tableRow);
        }
    }

    public void actionAddLanguage() {
        try {
            ArrayList<String> languages = new ArrayList<>();
            Object data = Config.get("LANGUAGES");
            if (!(data instanceof JSONArray)) return;

            for (int i = 0; i < ((JSONArray) data).length(); i++) {
                String languageCode = ((JSONArray) data).getJSONObject(i).getString("file_code");
                if (!JOIPackageManager.getInstance().getJoiPackageLanguages().contains(languageCode))
                    languages.add(languageCode);
            }

            String newLanguage = DialogRequestLanguage.requestLanguage(languages);
            if (newLanguage == null) return;

            JOIPackage joiPackage = Controller.getInstance().getJoiPackage().clone();
            joiPackage.setPackageLanguageCode(newLanguage);

            JOIPackageManager.getInstance().addJOIPackage(joiPackage);

            //Add to table
            initialize();
        } catch (CloneNotSupportedException e) {
            AsisUtils.errorDialogWindow(e);
        }
    }

    public void actionClose() {
        Stage stage = (Stage) tableViewJoi.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private static class TableRow<T> {
        ObservableList<T> rowData = FXCollections.observableArrayList();

        public ObservableList<T> getRowData() {
            return rowData;
        }

        public T getData(int index) {
            return getRowData().get(index);
        }
    }
}
