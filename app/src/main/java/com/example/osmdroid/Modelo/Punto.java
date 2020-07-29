package com.example.osmdroid.Modelo;

import java.io.Serializable;

public class Punto implements Serializable {

    private double latitud;
    private double longitud;
    private double pm;
    private String user;
    private String mac;
    private String time;

    public Punto(){    }

    public Punto(double longitud, double latitud, double pm, String user, String mac, String time){
        this.latitud = latitud;
        this.longitud = longitud;
        this.pm = pm;
        this.user = user;
        this.mac = mac;
        this.time = time;
    }

    public void setLatitud(double latitud){
        this.latitud = latitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public double getPm() {
        return pm;
    }

    public void setPm(double pm) {
        this.pm = pm;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
