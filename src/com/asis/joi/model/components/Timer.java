package com.asis.joi.model.components;

import com.asis.joi.model.JOISystemInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Timer implements JOISystemInterface, FirstLevelEffect {

    private int totalTime;
    private final ArrayList<Line> lineArrayList = new ArrayList<>();
    private boolean timerHidden=false, timeHidden=false;
    private String timerTextColor, timerTextOutlineColor;

    public void addNewLine(int lineNumber) {
        getLineArrayList().add(new Line(lineNumber));
    }
    public void removeLine(int lineNumber) {
        getLineArrayList().remove(getLine(lineNumber));
    }

    public JSONArray getTimerAsJson() {
        //Make new Object
        JSONObject timerObject = new JSONObject();

        //Set values
        timerObject.put("totalTime", getTotalTime());
        if(isTimeHidden()) timerObject.put("timeHidden", true);
        if(isTimerHidden()) timerObject.put("timerHidden", true);
        if(getTimerTextColor() != null && getTimerTextOutlineColor() != null) {
            timerObject.put("timerTextColor", getTimerTextColor());
            timerObject.put("timerTextOutlineColor", getTimerTextOutlineColor());
        }

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
        if (jsonObject.has("totalTime")) setTotalTime(jsonObject.getInt("totalTime"));

        //Set timeHidden
        if (jsonObject.has("timeHidden")) setTimeHidden(true);

        //Set timerHidden
        if (jsonObject.has("timerHidden")) setTimerHidden(true);

        //Set timerTextColor
        if (jsonObject.has("timerTextColor")) setTimerTextColor(jsonObject.getString("timerTextColor"));

        //Set timerTextOutlineColor
        if (jsonObject.has("timerTextOutlineColor")) setTimerTextOutlineColor(jsonObject.getString("timerTextOutlineColor"));

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
        if (isTimerHidden() != timer.isTimerHidden()) return false;
        if (isTimeHidden() != timer.isTimeHidden()) return false;
        if (getLineArrayList() != null ? !getLineArrayList().equals(timer.getLineArrayList()) : timer.getLineArrayList() != null)
            return false;
        if (getTimerTextColor() != null ? !getTimerTextColor().equals(timer.getTimerTextColor()) : timer.getTimerTextColor() != null)
            return false;
        return getTimerTextOutlineColor() != null ? getTimerTextOutlineColor().equals(timer.getTimerTextOutlineColor()) : timer.getTimerTextOutlineColor() == null;
    }

    //Getters and Setters
    public ArrayList<Line> getLineArrayList() {
        return lineArrayList;
    }

    public int getTotalTime() {
        return totalTime;
    }
    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public boolean isTimerHidden() {
        return timerHidden;
    }
    public void setTimerHidden(boolean timerHidden) {
        this.timerHidden = timerHidden;
    }

    public boolean isTimeHidden() {
        return timeHidden;
    }
    public void setTimeHidden(boolean timeHidden) {
        this.timeHidden = timeHidden;
    }

    public String getTimerTextColor() {
        return timerTextColor;
    }
    public void setTimerTextColor(String timerTextColor) {
        this.timerTextColor = timerTextColor;
    }

    public String getTimerTextOutlineColor() {
        return timerTextOutlineColor;
    }
    public void setTimerTextOutlineColor(String timerTextOutlineColor) {
        this.timerTextOutlineColor = timerTextOutlineColor;
    }
}
