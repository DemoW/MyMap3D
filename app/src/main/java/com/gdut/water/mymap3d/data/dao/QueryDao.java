package com.gdut.water.mymap3d.data.dao;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Default;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by Water on 2017/4/23.
 */
@Table("query")
public class QueryDao {

    @Column("_id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("category")
    private int category;

    @Column("name")
    private String name;

    @Column("latitude")
    private double latitude;

    @Column("longitude")
    private double longitude;

    @Column("type")
    @Default("0")
    private int type;

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
}
