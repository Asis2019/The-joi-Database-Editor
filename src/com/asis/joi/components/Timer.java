package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Timer implements JOISystemInterface {

    private int totalTime;
    private ArrayList<Line> lineArrayList = new ArrayList<>();

    public void addNewLine(int lineNumber) {
        getLineArrayList().add(new Line(lineNumber));
    }
    public boolean removeLine(int lineNumber) {
        return getLineArrayList().remove(getLine(lineNumber));
    }

    public JSONArray getTimerAsJson() {
        //Make new Object
        JSONObject timerObject = new JSONObject();

        //Set values
        timerObject.put("totalTime", getTotalTime());
        for(Line line: getLineArrayList()) {
            timerObject.put("line"+line.getLineNumber(), line.getLineAsJson());
        }

        JSONArray wrapper = new JSONArray();
        return wrapper.put(timerObject);
    }

    public Line getLine(int lineNumber) {
        for(Line line: getLineArrayList()) {
            if (line.getLineNumber() == lineNumber) {
                return line;
            }
        }
        return null;
    }

    @Override
    public void setDataFromJson(JSONObject jsonObject, File importDirectory) {
        //Set totalTime
        if (jsonObject.has("totalTime")) {
            setTotalTime(jsonObject.getInt("totalTime"));
        }

        //set lines
        for(int i=0; i<jsonObject.names().length(); i++) {
            String workingKey = jsonObject.names().getString(i);
            if(workingKey.matches("line.*")) {
                int lineSecond = Integer.parseInt(workingKey.replaceAll("[^0-9]", ""));
                addNewLine(lineSecond);
                getLine(lineSecond).setDataFromJson(jsonObject.getJSONArray(workingKey).getJSONObject(0), importDirectory);
            }
        }
    }

    @Override
    public String toString() {
        return getTimerAsJson().toString(4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timer)) return false;

        Timer timer = (Timer) o;

        if (getTotalTime() != timer.getTotalTime()) return false;
        return getLineArrayList().equals(timer.getLineArrayList());
    }

    //Getters and Setters
    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }
    public void setLineArrayList(ArrayList<Line> lineArrayList) {
        this.lineArrayList = lineArrayList;
    }

    public int getTotalTime() {
        return totalTime;
    }
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}
