package com.edu.bupt.camera.model;

public class Camera {
    private String id;

    private String serial;

    private String name;



    public Camera(String id, String serial, String name) {
        this.id = id;
        this.serial = serial;
        this.name = name;

    }

    public Camera(String serial, String name) {
        this.serial = serial;
        this.name = name;
    }

    public Camera() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial == null ? null : serial.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }
}