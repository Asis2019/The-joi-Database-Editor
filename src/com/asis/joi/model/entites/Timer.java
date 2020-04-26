package com.asis.joi.model.entites;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.ArrayList;

public class Timer implements JSONString, JOIEntity<JSONArray>, Cloneable {

    private int totalTime;
    private ArrayList<Line> lineArrayList = new ArrayList<>();
    private boolean timerHidden=false, timeHidden=false;
    private String timerTextColor, timerTextOutlineColor;

    public void addNewLine(int lineNumber) {
        JSONObject lineObject = new JSONObject();
        lineObject.put("id", lineNumber);

        getLineArrayList().add(Line.createEntity(lineObject));
    }
    public void removeLine(int lineNumber) {
        getLineArrayList().remove(getLine(lineNumber));
    }

    public Line getLine(int lineNumber) {
        for(Line line: getLineArrayList())
            if (line.getLineNumber() == lineNumber) return line;
        return null;
    }

    public static Timer createEntity(JSONObject jsonObject) {
        Timer timer = new Timer();

        //Set totalTime
        if (jsonObject.has("totalTime")) timer.setTotalTime(jsonObject.getInt("totalTime"));

        //Set timeHidden
        if (jsonObject.has("timeHidden")) timer.setTimeHidden(true);

        //Set timerHidden
        if (jsonObject.has("timerHidden")) timer.setTimerHidden(true);

        //Set timerTextColor
        if (jsonObject.has("timerTextColor")) timer.setTimerTextColor(jsonObject.getString("timerTextColor"));

        //Set timerTextOutlineColor
        if (jsonObject.has("timerTextOutlineColor")) timer.setTimerTextOutlineColor(jsonObject.getString("timerTextOutlineColor"));

        //set lines
        for(int i=0; i<jsonObject.names().length(); i++) {
            String workingKey = jsonObject.names().getString(i);
            if(workingKey.matches("line.*")) {
                int lineSecond = Integer.parseInt(workingKey.replaceAll("[^0-9]", ""));

                JSONObject lineObject = jsonObject.getJSONArray("line" + lineSecond).getJSONObject(0);
                lineObject.put("id", i);

                timer.getLineArrayList().add(Line.createEntity(lineObject));
            }
        }

        return timer;
    }

    @Override
    public JSONArray toJSON() {
        JSONObject timerObject = new JSONObject();

        timerObject.put("totalTime", getTotalTime());
        if(isTimeHidden()) timerObject.put("timeHidden", true);
        if(isTimerHidden()) timerObject.put("timerHidden", true);
        if(getTimerTextColor() != null && getTimerTextOutlineColor() != null) {
            timerObject.put("timerTextColor", getTimerTextColor());
            timerObject.put("timerTextOutlineColor", getTimerTextOutlineColor());
        }

        for(Line line: getLineArrayList()) {
            timerObject.put("line"+line.getLineNumber(), line.toJSON());
        }

        JSONArray wrapper = new JSONArray();
        return wrapper.put(timerObject);
    }

    @Override
    public String toJSONString() {
        return toJSON().toString(4);
    }

    @Override
    public Timer clone() throws CloneNotSupportedException {
        Timer timer = (Timer) super.clone();

        ArrayList<Line> clonedArray = new ArrayList<>();
        for (Line line: getLineArrayList()) clonedArray.add(line.clone());
        timer.setLineArrayList(clonedArray);

        timer.setTimerTextOutlineColor(getTimerTextOutlineColor());
        timer.setTimerTextColor(getTimerTextColor());
        timer.setTimeHidden(isTimeHidden());
        timer.setTimerHidden(isTimerHidden());
        timer.setTotalTime(getTotalTime());

        return timer;
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
    private void setLineArrayList(ArrayList<Line> lineArrayList) {
        this.lineArrayList = lineArrayList;
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
