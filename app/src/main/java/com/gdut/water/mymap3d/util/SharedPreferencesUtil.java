package com.gdut.water.mymap3d.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.amap.api.services.core.LatLonPoint;

/**
 * Created by Water on 2017/4/7.
 */

public class SharedPreferencesUtil {

    private Context mContext;
    //sp存储
//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;

    public SharedPreferencesUtil(Context context){
        mContext = context;
    }
    //初始化
    public void init(Context context){
        mContext =context;
        //sp初始化
//        sharedPreferences = context.getSharedPreferences(Constants.BASIC_MAP_DATA, Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
    }

    public void setParam(String key,String value){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public void setParam(String key,int value){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public void setParam(String key,boolean value){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public String getParam(String key, String defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }
    public boolean getParam(String key, boolean defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public int getParam(String key, int defaultValue){
        SharedPreferences sp = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA,
                Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }
    //获取当前坐标
    public LatLonPoint getCurrentPoint(){

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA, Context.MODE_PRIVATE);
        double latitude = Double.parseDouble(sharedPreferences.getString(Constants.CURRENT_LATITUDE,null));
        double longitude = Double.parseDouble(sharedPreferences.getString(Constants.CURRENT_LONGITUDE,null));
        LatLonPoint latLonPoint = new LatLonPoint(latitude,longitude);
        return latLonPoint;
    }

    //获取目标坐标点
    public LatLonPoint getDestinyPoint(){

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.BASIC_MAP_DATA, Context.MODE_PRIVATE);
        double latitude = Double.parseDouble(sharedPreferences.getString(Constants.DESTINY_LATITUDE,null));
        double longitude = Double.parseDouble(sharedPreferences.getString(Constants.DESTINY_LONGITUDE,null));
        LatLonPoint latLonPoint = new LatLonPoint(latitude,longitude);
        return latLonPoint;
    }

//    //获取当前坐标的地址
//    public String getCurrentAddress(){
//        String address;
//        address = sharedPreferences.getString(Constants.CURRENT_SITE,null);
//        return address;
//    }
//
//    //获取搜索目标的地址
//    public String getDestinyAddress(){
//        String address;
//        address = sharedPreferences.getString(Constants.ADDRESS_SEARCH,null);
//        return address;
//    }
//
//    //设置目标坐标点
//    public void setDestinyPoint(double latitude , double longitude){
//
//        editor.putString(Constants.DESTINY_LATITUDE,String.valueOf(latitude));
//        editor.putString(Constants.DESTINY_LONGITUDE,String.valueOf(longitude));
//        editor.commit();
//    }
//
//    //设置目标坐标点通过LatLonPoint
//    public void setDestinyPointByClass(LatLonPoint latLonPoint){
//
//        editor.putString(Constants.DESTINY_LATITUDE,String.valueOf(latLonPoint.getLatitude()));
//        editor.putString(Constants.DESTINY_LONGITUDE,String.valueOf(latLonPoint.getLongitude()));
//        editor.commit();
//    }
//    ////设置公交和驾车策略
//    public void setRouteMode(int bus,int driving){
//        editor.putInt(Constants.BUS_MODE,bus);
//        editor.putInt(Constants.DRIVING_MODE,driving);
//        editor.commit();
//    }
//
//    //获取当前公交策略
//    public int[] getRouteMode(){
//        int[] mode = new int[2];
//        mode[0] = sharedPreferences.getInt(Constants.BUS_MODE,0);
//        mode[1] = sharedPreferences.getInt(Constants.DRIVING_MODE,2);
//        return mode;
//    }


}
