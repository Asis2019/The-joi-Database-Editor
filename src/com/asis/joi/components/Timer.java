package com.asis.joi.components;

import com.asis.joi.JOISystemInterface;
import org.json.JSONObject;

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

    public JSONObject getTimerAsJson() {
        //Make new Object
        JSONObject timerObject = new JSONObject();

        //Set values
        timerObject.put("totalTime", getTotalTime());
        for(Line line: getLineArrayList()) {
            timerObject.put("line"+line.getLineNumber(), line.getLineAsJson());
        }

        return timerObject;
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
    public void setDataFromJson(JSONObject jsonObject) {
        //Set totalTime
        if (jsonObject.has("totalTime")) {
            setTotalTime(jsonObject.getInt("totalTime"));
        }

        //set lines
        for(int i=0; i<jsonObject.names().length(); i++) {
            String workingKey = jsonObject.names().getString(i);
            if(workingKey.matches("^line")) {
                int lineSecond = workingKey.charAt(workingKey.length()-1);
                addNewLine(lineSecond);
                getLine(lineSecond).setDataFromJson(jsonObject.getJSONArray(workingKey).getJSONObject(0));
            }
        }
    }

    @Override
    public String toString() {
        return getTimerAsJson().toString(4);
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
