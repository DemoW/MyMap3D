package com.gdut.water.mymap3d.util;

import android.content.Context;

import com.gdut.water.mymap3d.data.dao.HistoryDao;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DbUtils {

    public static String DB_NAME;
    public static LiteOrm liteOrm;

    public static void openDB(Context context, String DB_NAME) {
        DB_NAME = DB_NAME + ".db";
        if (liteOrm == null) {
            liteOrm = LiteOrm.newCascadeInstance(context, DB_NAME);
            liteOrm.setDebugged(true);
        }
    }


    public static LiteOrm getLiteOrm() {
        return liteOrm;
    }

    //考虑到Orm的实用性，所以不排除使用创建表、插入和查询这几个方法
    /**
     * 插入一条记录
     *
     * @param t
     */
    public static <T> void insert(T t) {
        liteOrm.save(t);
    }

    /**
     * 插入所有记录
     *
     * @param list
     */
    public static <T> void insertAll(List<T> list) {
        liteOrm.save(list);
    }

    /**
     * 查询所有
     *
     * @param cla
     * @return
     */
    public static <T> List<T> getQueryAll(Class<T> cla) {
        return liteOrm.query(cla);
    }

    /**
     * 查询  某字段 等于 Value的值
     *
     * @param cla
     * @param field
     * @param value
     * @return
     */
    public static <T> List<T> getQueryByWhere(Class<T> cla, String field, String value) {
        return liteOrm.<T>query(new QueryBuilder(cla).where(field + "=?", value));
    }

    /**
     * 查询  某字段 等于 Value的值  可以指定从1-20，就是分页
     *
     * @param cla
     * @param field
     * @param value
     * @param start
     * @param length
     * @return
     */
    public static <T> List<T> getQueryByWhereLength(Class<T> cla, String field, String value, int start, int length) {
        return liteOrm.<T>query(new QueryBuilder(cla).where(field + "=?", value).limit(start, length));
    }

    private static String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private static String getOldDate(){
        Calendar c = Calendar.getInstance();//默认是当前日期
        c.add(Calendar.DATE,-7);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(c.getTime());
    }
    /*
    *查询history表一周的历史记录情况
    */
    public static List<HistoryDao> getQueryInWeek(){
        QueryBuilder q = new QueryBuilder(HistoryDao.class);
        q.whereGreaterThan("createTime",getOldDate())
                .whereAppendAnd()
                .whereLessThan("createTime",getTodayDate());
        return getLiteOrm().query(q);
//        String sql = "SELECT * FROM location WHERE location.createTime<(SELECT datetime('now','localtime')) " +
//                "AND location.createTime >=(SELECT datetime('now','localtime','-7 day'))";
//        Cursor cursor = db.rawQuery(sql,null);
//        while(cursor.moveToNext()){
//
//        }
    }

    /**
     * 删除某张表里的所有数据
     *
     */
    public static <T> void deleteAll(Class<T> cla) {

//        SQLiteDatabase db = getLiteOrm().getWritableDatabase();
//        db.beginTransaction();
//        try{
//            db.execSQL("DELETE FROM "+tableName);
//            db.setTransactionSuccessful();
//        }catch (SQLException sql){
//            sql.printStackTrace();
//        }finally {
//            db.endTransaction();
//        }

        liteOrm.deleteAll(cla);

    }

    public static void closeDb(){
        liteOrm.close();
    }

}
