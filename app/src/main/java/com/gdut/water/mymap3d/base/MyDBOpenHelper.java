package com.gdut.water.mymap3d.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Water on 2017/4/19.
 */

public class MyDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME_QUERY_LOG = "querylog";
    private static final String TABLE_CREATE_Query =
            "CREATE TABLE " + TABLE_NAME_QUERY_LOG + " (" +
                    "id integer primary key autoincrement, "+
                    "content text, "+
                    "input_time text)";

    private  Context mContext;
    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE_Query);
        Toast.makeText(mContext,"创建数据库成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
