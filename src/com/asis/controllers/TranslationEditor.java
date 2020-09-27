package com.asis.controllers;

import com.asis.controllers.dialogs.DialogMessage;
import com.asis.controllers.dialogs.DialogRequestLanguage;
import com.asis.joi.JOIPackageManager;
import com.asis.joi.model.JOIPackage;
import com.asis.joi.model.entities.*;
import com.asis.joi.model.entities.dialog.Dialog;
import com.asis.joi.model.entities.dialog.DialogOption;
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
import java.util.Collections;
import java.util.Comparator;

public class TranslationEditor {

    @FXML
    private TableView<TableRow<SceneComponent<?>>> tableViewJoi;

    private int rowIndex = 0;

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
        ArrayList<String> joiPackageLanguages = new ArrayList<>(JOIPackageManager.getInstance().getJoiPackageLanguages());
        Collections.sort(joiPackageLanguages);

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

        tableColumn.setOnEditCommit(e -> {
            TableRow<SceneComponent<?>> tableRow = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if (tableRow.getRowData().size() - 1 >= dataIndex) {
                SceneComponent<?> entity = tableRow.getRowData().get(dataIndex);
                if (entity instanceof Line) ((Line) entity).setText(e.getNewValue());
                else if (entity instanceof DialogOption) ((DialogOption) entity).setOptionText(e.getNewValue());
                else if (entity instanceof Transition) ((Transition) entity).setTransitionText(e.getNewValue());
            } else {
                DialogMessage.messageDialog("Error", "The field was unable to update, because the line/scene data structure is missing in the joi_text file." +
                        " Please make sure all joi_text files have the same structure before attempting to translate.\n");
                e.consume();
            }
            tableViewJoi.requestFocus();
        });

        tableViewJoi.getColumns().add(tableColumn);
    }

    private void loadJoiTableData() {
        //TODO split creation of header column (col 0) and the rest of the columns
        //TODO Loading should be changed to inserting of rows instead of columns
        ObservableList<TableRow<SceneComponent<?>>> itemsList = FXCollections.observableArrayList();

        ArrayList<JOIPackage> joiPackages = JOIPackageManager.getInstance().getJoiPackages();
        joiPackages.sort(Comparator.comparing(JOIPackage::getPackageLanguageCode));
        for (int columnIndex = 1; columnIndex <= joiPackages.size(); columnIndex++) {
            JOIPackage joiPackage = joiPackages.get(columnIndex - 1);

            rowIndex = 0;
            for (JOIComponent component : joiPackage.getJoi().getJoiComponents()) {
                if(component instanceof Scene) {
                    Scene scene = (Scene) component;
                    //Add normal text
                    for (Line line : scene.getComponent(LineGroup.class).getLineArrayList()) {
                        addDataToCell(itemsList, columnIndex, scene.getComponentTitle() + " - Line " + line.getLineNumber(), line);
                    }

                    //Add text from timers options
                    if (scene.hasComponent(Timer.class)) {
                        for (Line line : scene.getComponent(Timer.class).getLineGroup().getLineArrayList()) {
                            addDataToCell(itemsList, columnIndex, scene.getComponentTitle() + " - Timer - Line " + line.getLineNumber(), line);
                        }
                    }

                    //Add text from dialog options
                    if (scene.hasComponent(Dialog.class)) {
                        for (DialogOption dialogOption : scene.getComponent(Dialog.class).getOptionArrayList()) {
                            addDataToCell(itemsList, columnIndex, scene.getComponentTitle() + " - Dialog - Option " + dialogOption.getOptionNumber(), dialogOption);
                        }
                    }

                    //Add text from transitions
                    if (scene.hasComponent(Transition.class) && scene.getComponent(Transition.class).getTransitionText() != null) {
                        addDataToCell(itemsList, columnIndex, scene.getComponentTitle() + " - Transition", scene.getComponent(Transition.class));
                    }
                }
            }
        }

        tableViewJoi.setItems(itemsList);
    }

    private void addDataToCell(ObservableList<TableRow<SceneComponent<?>>> itemsList, int columnIndex, String cellData, SceneComponent<?> entity) {
        final boolean newRowNeeded = itemsList.size() - 1 < rowIndex;

        //Creates the first column (col 0)
        if (newRowNeeded && columnIndex != 1) {
            //Second language contains more rows than first
            TableRow<SceneComponent<?>> tableRow = new TableRow<>();
            tableRow.getRowData().add(0, Line.createEntity(new JSONObject("{\"text\":\"\"}")));
            for (int i = 1; i <= columnIndex - 1; i++)
                tableRow.getRowData().add(i, Line.createEntity(new JSONObject("{\"text\":\"\"}")));

            itemsList.add(tableRow);

        } else if (newRowNeeded) {
            TableRow<SceneComponent<?>> tableRow = new TableRow<>();
            tableRow.getRowData().add(0, Line.createEntity(new JSONObject("{\"text\":\"" + cellData + "\"}")));
            itemsList.add(tableRow);
        }

        //Adds data to language columns (col 1+)
        TableRow<SceneComponent<?>> tableRow = itemsList.get(rowIndex);
        tableRow.getRowData().add(columnIndex, entity);

        rowIndex++;
    }

    private String getTextFromEntity(SceneComponent<?> entity) {
        if (entity instanceof Line) return ((Line) entity).getText();
        else if (entity instanceof DialogOption) return ((DialogOption) entity).getOptionText();
        else if (entity instanceof Transition) return ((Transition) entity).getTransitionText();
        else return null;
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
