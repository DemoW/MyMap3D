package com.gdut.water.mymap3d.data.dao;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;

/**
 * 为地理编码后的结果进行保存
 * 地理编码:将具体的地址信息转为经纬度
 * Created by Water on 2017/4/23.
 */

@Table("geocoder")
public class GeocoderDao {

    @Column("_id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("name")
    private String name;

    @Column("latitude")
    private double latitude;

    @Column("longitude")
    private double longitude;

    @Column("city")
    private String city;

    @Column("address")
    @Unique
    private String address;

    @Column("distract")
    private String distract;

    @Column("building")
    private String building;

    @Column("neighborhood")
    private String neighborhood;

    @Column("category")
    private int category;

    @Column("createTime")
    private String createTime;

    @Column("updateTime")
    private String updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistract() {
        return distract;
    }

    public void setDistract(String distract) {
        this.distract = distract;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
