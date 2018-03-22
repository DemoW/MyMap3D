package com.gdut.water.mymap3d.data.dao;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by Water on 2017/4/23.
 */

@Table("category")
public class CategoryDao {

    /*
    * 为地理编码或者逆地理编码之后的结果进行类别判断
    */
    @Column("_id")
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("type")
    private int type;

    @Column("content")
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
