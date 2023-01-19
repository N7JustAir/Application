package com.justairapp;

import java.util.List;

public class Sensor {

    private String idSensor;

    private String roomName;

    private List<Gaz> gaz;

    public Sensor(String idSensor, String roomName) {
        this.idSensor = idSensor;
        this.roomName = roomName;
    }

    public Sensor(String idSensor, String roomName, List<Gaz> gaz) {
        this.idSensor = idSensor;
        this.roomName = roomName;
        this.gaz = gaz;
    }

    public String getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(String idSensor) {
        this.idSensor = idSensor;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<Gaz> getGaz() {
        return gaz;
    }

    public void setGaz(List<Gaz> gaz) {
        this.gaz = gaz;
    }

    public String toString() {
        String info = "";
        if (gaz != null) {
            for (Gaz g : gaz) {
                info += "-" + g.toString() + "\n";
            }
        }
        return this.idSensor + " " + this.roomName + " :\n" + info;
    }
}
