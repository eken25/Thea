package com.prdeck.thea;

public class AppData {
    private String packageName;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private int category;
    private double lat;
    private double lon;
    private String ua;
    AppData(){

    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String toString(){
        return packageName + "," + startTime + "," + endTime + "," + duration + "," + category;
    }
}
