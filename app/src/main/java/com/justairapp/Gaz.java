package com.justairapp;

import java.util.List;

public class Gaz {

    private String gazName;

    private List<Measure> measures;

    public Gaz(String gazName) {
        this.gazName = gazName;
    }

    public Gaz(String gazName, List<Measure> measures) {
        this.gazName = gazName;
        this.measures = measures;
    }

    public String getGazName() {
        return gazName;
    }

    public void setGazName(String gazName) {
        this.gazName = gazName;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public String toString() {
        String info = "";
        for (Measure m : measures) {
            info += m.toString() + " - ";
        }
        return gazName + "\n" + info;
    }
}
