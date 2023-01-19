package com.justairapp;

public class Measure {

    private int timestamp;

    private float value;

    public Measure(int timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String toString() {
        return "at " + timestamp + " : " + value;
    }
}
