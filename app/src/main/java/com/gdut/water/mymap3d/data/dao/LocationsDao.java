package com.gdut.water.mymap3d.data.dao;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by Water on 2017/4/21.
 */

@Table("location")
public class LocationsDao {

    @Column("_id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int _id;

    @Column("name")
    private String name; //名字不唯一，记录热点搜索地方

    @Column("latitude")
    private double latitude;

    @Column("longitude")
    private double longitude;

    @Column("address")
    private String address;

    @Column("createTime")
    @NotNull
    private String createTime;

    @Column("distract")
    private String distract;

    @Column("poiID")
    private String poiID;

    @Column("typeCode")
    private String typeCode;

    @Column("updateTime")
    private String updateTime;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDistract() {
        return distract;
    }

    public void setDistract(String distract) {
        this.distract = distract;
    }

    public String getPoiID() {
        return poiID;
    }

    public void setPoiID(String poiID) {
        this.poiID = poiID;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "location{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", createTime='" + createTime + '\'' +
                ", distract='" + distract + '\'' +
                ", poiID='" + poiID + '\'' +
                ", typeCode='" + typeCode + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
