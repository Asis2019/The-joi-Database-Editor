package com.asis.joi.model.entities;

import com.asis.utilities.AsisUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

public class Timer implements JSONString, SceneComponent<JSONArray> {

    private int totalTime;
    private LineGroup lineGroup = new LineGroup();
    private boolean timerHidden=false, timeHidden=false;
    private String timerTextColor, timerTextOutlineColor;

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
        for(String key: jsonObject.keySet()) {
            if(key.matches("line.*")) {
                int lineSecond = Integer.parseInt(key.replaceAll("[^0-9]", ""));
                timer.getLineGroup().getLineArrayList().add(Line.createEntity(jsonObject.getJSONArray("line" + lineSecond).getJSONObject(0), lineSecond));
            }
        }

        return timer;
    }

    public double getDuration() {
        return getTotalTime();
    }

    @Override
    public String jsonKeyName() {
        return "timer";
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

        if(getLineGroup().getLineArrayList().size() > 0)
            timerObject = AsisUtils.mergeObject(timerObject, getLineGroup().toJSON());

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

        timer.setLineGroup(getLineGroup().clone());
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
        if (!getLineGroup().equals(timer.getLineGroup())) return false;
        if (getTimerTextColor() != null ? !getTimerTextColor().equals(timer.getTimerTextColor()) : timer.getTimerTextColor() != null)
            return false;
        return getTimerTextOutlineColor() != null ? getTimerTextOutlineColor().equals(timer.getTimerTextOutlineColor()) : timer.getTimerTextOutlineColor() == null;
    }

    //Getters and Setters
    public LineGroup getLineGroup() {
        return lineGroup;
    }
    private void setLineGroup(LineGroup lineGroup) {
        this.lineGroup = lineGroup;
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
