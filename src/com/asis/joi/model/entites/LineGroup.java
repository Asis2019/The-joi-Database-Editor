package com.asis.joi.model.entites;

import org.json.JSONObject;
import org.json.JSONString;

import java.util.ArrayList;

public class LineGroup implements JSONString, SceneComponent<JSONObject> {

    private ArrayList<Line> lineArrayList = new ArrayList<>();

    public void addNewLine(int lineNumber) {
        getLineArrayList().add(Line.createEntity(new JSONObject(), lineNumber));
    }

    public void removeLine(int lineNumber) {
        getLineArrayList().remove(getLine(lineNumber));
    }

    public Line getLine(int lineNumber) {
        for(Line line: getLineArrayList())
            if (line.getLineNumber() == lineNumber) return line;
        return null;
    }

    @Override
    public String jsonKeyName() {
        return null;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject lineGroupObject = new JSONObject();
        for(Line line: getLineArrayList()) lineGroupObject.put("line" + line.getLineNumber(), line.toJSON());
        return lineGroupObject;
    }

    @Override
    public double getDuration() {
        return getLineArrayList().stream().mapToDouble(Line::getDuration).sum();
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public LineGroup clone() throws CloneNotSupportedException {
        LineGroup lineGroup = (LineGroup) super.clone();

        ArrayList<Line> clonedArray = new ArrayList<>();
        for (Line line: getLineArrayList()) clonedArray.add(line.clone());
        lineGroup.setLineArrayList(clonedArray);

        return lineGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineGroup)) return false;

        LineGroup lineGroup = (LineGroup) o;

        return getLineArrayList().equals(lineGroup.getLineArrayList());
    }

    //Getters and Setters
    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }
    private void setLineArrayList(ArrayList<Line> lineArrayList) {
        this.lineArrayList = lineArrayList;
    }
}