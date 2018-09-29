package com.valjapan.vendor;

public class PlaceData {
    public String vendingKind;
    public String content;
    public double locateX;
    public double locateY;
    public String fireBaseKey;

    public PlaceData(String fireBaseKey, String vendingKind, String content, double locateX, double locateY) {
        this.fireBaseKey = fireBaseKey;
        this.vendingKind = vendingKind;
        this.content = content;
        this.locateX = locateX;
        this.locateY = locateY;
    }

    public PlaceData() {

    }

    public String getVendingKind() {
        return vendingKind;
    }

    public void setVendingKind(String vendingKind) {
        this.vendingKind = vendingKind;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFireBaseKey() {
        return fireBaseKey;
    }

    public void setFireBaseKey(String fireBaseKey) {
        this.fireBaseKey = fireBaseKey;
    }

    public double getLocateX() {
        return locateX;
    }

    public void setLocateX(double locateX) {
        this.locateX = locateX;
    }

    public double getLocateY() {
        return locateY;
    }

    public void setLocateY(double locateY) {
        this.locateY = locateY;
    }
}
