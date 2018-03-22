package com.gdut.water.mymap3d.data.pojo;

/**
 * Created by Water on 2017/4/22.
 */

public class Location {

    private String name;
    private String distract;
    private double latitude;
    private double longitude;
    private String createTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistract() {
        return distract;
    }

    public void setDistract(String distract) {
        this.distract = distract;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
