package com.asis.ui;

import javafx.scene.control.TextField;

public class NumberField extends TextField {
    public enum FormatType {
        DECIMAL,
        INTEGER
    }

    private FormatType formatType = FormatType.DECIMAL;

    public NumberField() {
        textProperty().addListener((observableValue, s, t1) -> {
            try {
                //Attempts to convert text into double value
                switch (formatType) {
                    case DECIMAL:
                        Double.parseDouble(t1);
                        break;
                    case INTEGER:
                        Integer.parseInt(t1);
                        break;
                }
            } catch (NumberFormatException e) {
                if (t1.isEmpty()) {
                    clear();
                    return;
                }
                final String backspacedText = t1.substring(0, t1.length() - 1);
                setText(backspacedText);
            }
        });
    }

    public Double getDoubleNumber() {
        return getDoubleNumber(null);
    }

    public Double getDoubleNumber(Double defaultValue) {
        if(getText().isEmpty()) return defaultValue;
        return Double.parseDouble(getText());
    }

    public Integer getIntegerNumber() {
        return getIntegerNumber(null);
    }

    public Integer getIntegerNumber(Integer defaultValue) {
        if(getText().isEmpty()) return defaultValue;
        return Integer.parseInt(getText());
    }

    public FormatType getFormatType() {
        return formatType;
    }
    public void setFormatType(FormatType formatType) {
        this.formatType = formatType;
    }
}
